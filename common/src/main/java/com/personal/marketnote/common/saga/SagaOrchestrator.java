package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.saga.exception.SagaDefinitionNotFoundException;
import com.personal.marketnote.common.saga.exception.SagaInstanceNotFoundException;
import com.personal.marketnote.common.saga.exception.SagaSerializationException;
import com.personal.marketnote.common.saga.exception.SagaStepNotFoundException;
import com.personal.marketnote.common.saga.port.FindSagaPort;
import com.personal.marketnote.common.saga.port.SaveSagaPort;
import com.personal.marketnote.common.saga.port.UpdateSagaPort;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@Slf4j
public class SagaOrchestrator {

    private static final String SOURCE = "saga-orchestrator";

    private final SaveSagaPort saveSagaPort;
    private final FindSagaPort findSagaPort;
    private final UpdateSagaPort updateSagaPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, SagaDefinition<?>> definitionRegistry;

    public SagaOrchestrator(SaveSagaPort saveSagaPort,
                            FindSagaPort findSagaPort,
                            UpdateSagaPort updateSagaPort,
                            SaveOutboxEventPort saveOutboxEventPort,
                            ObjectMapper objectMapper,
                            Clock clock,
                            List<SagaDefinition<?>> definitions) {
        this.saveSagaPort = saveSagaPort;
        this.findSagaPort = findSagaPort;
        this.updateSagaPort = updateSagaPort;
        this.saveOutboxEventPort = saveOutboxEventPort;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.definitionRegistry = new ConcurrentHashMap<>();
        definitions.forEach(d -> this.definitionRegistry.put(d.getSagaType(), d));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public <T> SagaInstance start(SagaDefinition<T> definition, String sagaId, T context) {
        String payload = serialize(context);

        SagaInstance instance = SagaInstance.from(
                new SagaInstanceCreateState(sagaId, definition.getSagaType(), payload), clock);
        SagaInstance savedInstance = saveSagaPort.save(instance);

        savedInstance.process();
        updateSagaPort.update(savedInstance);

        processStep(definition, savedInstance, context, 0);

        return savedInstance;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleStepResponse(String sagaId, String stepName, boolean success, String response) {
        SagaInstance instance = findSagaInstance(sagaId);

        if (instance.isTerminal()) {
            return;
        }

        List<SagaStep> steps = findSagaPort.findStepsBySagaInstanceId(instance.getId());
        SagaStep currentStep = findStepByName(steps, stepName);

        if (!currentStep.isProcessing()) {
            return;
        }

        if (success) {
            handleStepSuccess(instance, currentStep, response);
            return;
        }
        handleStepFailure(instance, steps, currentStep, response);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleCompensationResponse(String sagaId, String stepName, boolean success, String response) {
        SagaInstance instance = findSagaInstance(sagaId);

        if (instance.isTerminal()) {
            return;
        }

        List<SagaStep> steps = findSagaPort.findStepsBySagaInstanceId(instance.getId());
        SagaStep step = findStepByName(steps, stepName);

        if (!step.isCompensating()) {
            return;
        }

        if (success) {
            handleCompensationSuccess(instance, steps, step, response);
            return;
        }
        handleCompensationFailure(instance, step, response);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleProcessingTimeout(String sagaId) {
        SagaInstance instance = findSagaInstance(sagaId);

        if (instance.isTerminal()) {
            return;
        }
        if (!instance.isProcessing()) {
            return;
        }

        List<SagaStep> steps = findSagaPort.findStepsBySagaInstanceId(instance.getId());
        Optional<SagaStep> currentStepOpt = findProcessingStep(steps);
        if (currentStepOpt.isEmpty()) {
            return;
        }
        SagaStep currentStep = currentStepOpt.get();

        log.warn("SAGA PROCESSING 타임아웃 처리. sagaId={}, sagaType={}, stepName={}",
                instance.getSagaId(), instance.getSagaType(), currentStep.getStepName());

        currentStep.fail("TIMEOUT");
        updateSagaPort.updateStep(currentStep);

        startCompensation(instance, steps);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleCompensationTimeout(String sagaId) {
        SagaInstance instance = findSagaInstance(sagaId);

        if (instance.isTerminal()) {
            return;
        }
        if (!instance.isCompensating()) {
            return;
        }

        List<SagaStep> steps = findSagaPort.findStepsBySagaInstanceId(instance.getId());
        List<SagaStep> compensatingSteps = steps.stream()
                .filter(SagaStep::isCompensating)
                .toList();

        for (SagaStep step : compensatingSteps) {
            step.failCompensation("COMPENSATION_TIMEOUT");
            updateSagaPort.updateStep(step);
        }

        instance.failCompensation();
        updateSagaPort.update(instance);

        log.warn("SAGA COMPENSATING 타임아웃 처리. sagaId={}, sagaType={}, 관리자 개입이 필요합니다.",
                instance.getSagaId(), instance.getSagaType());
    }

    private void handleStepSuccess(SagaInstance instance, SagaStep currentStep, String response) {
        currentStep.succeed(response);
        updateSagaPort.updateStep(currentStep);

        processNextStepOrComplete(instance, currentStep.getStepIndex());
    }

    private void handleStepFailure(SagaInstance instance, List<SagaStep> steps,
                                   SagaStep currentStep, String response) {
        currentStep.fail(response);
        updateSagaPort.updateStep(currentStep);

        startCompensation(instance, steps);
    }

    private void handleCompensationSuccess(SagaInstance instance, List<SagaStep> steps,
                                           SagaStep step, String response) {
        step.completeCompensation(response);
        updateSagaPort.updateStep(step);

        boolean allCompensated = steps.stream().noneMatch(SagaStep::isCompensating);
        if (allCompensated) {
            instance.compensate(clock);
            updateSagaPort.update(instance);
        }
    }

    private void handleCompensationFailure(SagaInstance instance, SagaStep step, String response) {
        step.failCompensation(response);
        updateSagaPort.updateStep(step);

        instance.failCompensation();
        updateSagaPort.update(instance);
    }

    private <T> void processStep(SagaDefinition<T> definition, SagaInstance instance,
                                 T context, int stepIndex) {
        SagaStepDefinition<T> stepDef = definition.getSteps().get(stepIndex);
        String actionRequest = stepDef.action().apply(context);

        SagaStep step = SagaStep.from(
                new SagaStepCreateState(instance.getId(), stepDef.stepName(), stepIndex, actionRequest),
                clock);
        step.process();
        saveSagaPort.saveStep(step);

        publishActionMessage(instance, stepDef, actionRequest);
    }

    @SuppressWarnings("unchecked")
    private <T> void processNextStepOrComplete(SagaInstance instance, int completedStepIndex) {
        SagaDefinition<T> definition = (SagaDefinition<T>) getDefinition(instance.getSagaType());
        int nextIndex = completedStepIndex + 1;

        if (nextIndex < definition.getSteps().size()) {
            instance.advanceStep();
            updateSagaPort.update(instance);

            T context = deserialize(instance.getPayload(), definition.getContextType());
            processStep(definition, instance, context, nextIndex);
            return;
        }
        instance.complete(clock);
        updateSagaPort.update(instance);
    }

    @SuppressWarnings("unchecked")
    private <T> void startCompensation(SagaInstance instance, List<SagaStep> steps) {
        instance.fail();
        instance.rollback();
        updateSagaPort.update(instance);

        List<SagaStep> compensatableSteps = steps.stream()
                .filter(SagaStep::requiresCompensation)
                .sorted(Comparator.comparingInt(SagaStep::getStepIndex).reversed())
                .toList();

        if (compensatableSteps.isEmpty()) {
            instance.compensate(clock);
            updateSagaPort.update(instance);
            return;
        }

        SagaDefinition<T> definition = (SagaDefinition<T>) getDefinition(instance.getSagaType());
        T context = deserialize(instance.getPayload(), definition.getContextType());

        for (SagaStep step : compensatableSteps) {
            SagaStepDefinition<T> stepDef = findStepDefinition(definition, step.getStepName());
            if (stepDef.hasCompensation()) {
                String compensationRequest = stepDef.compensation().apply(context);
                step.compensate(compensationRequest);
                updateSagaPort.updateStep(step);
                publishCompensationMessage(instance, stepDef, compensationRequest);
                continue;
            }
            step.compensate("NO_COMPENSATION_REQUIRED");
            step.completeCompensation("NO_COMPENSATION_REQUIRED");
            updateSagaPort.updateStep(step);
        }
    }

    private <T> void publishActionMessage(SagaInstance instance, SagaStepDefinition<T> stepDef,
                                          String request) {
        SagaStepMessage message = new SagaStepMessage(
                instance.getSagaId(), instance.getSagaType(), stepDef.stepName(),
                SagaStepMessage.ACTION, request);
        String eventType = "saga." + instance.getSagaType() + "." + stepDef.stepName() + ".action";
        publishToOutbox(stepDef.topic(), instance.getSagaId(), eventType, message);
    }

    private <T> void publishCompensationMessage(SagaInstance instance, SagaStepDefinition<T> stepDef,
                                                String request) {
        SagaStepMessage message = new SagaStepMessage(
                instance.getSagaId(), instance.getSagaType(), stepDef.stepName(),
                SagaStepMessage.COMPENSATION, request);
        String eventType = "saga." + instance.getSagaType() + "." + stepDef.stepName() + ".compensation";
        publishToOutbox(stepDef.topic(), instance.getSagaId(), eventType, message);
    }

    private void publishToOutbox(String topic, String partitionKey, String eventType, Object message) {
        EventEnvelope<Object> envelope = EventEnvelope.of(eventType, SOURCE, message, clock);
        String envelopeJson = serialize(envelope);
        OutboxEvent event = OutboxEvent.of(
                envelope.eventId(), topic, partitionKey,
                envelope.eventType(), SOURCE, envelopeJson, clock);
        saveOutboxEventPort.save(event);
    }

    private SagaInstance findSagaInstance(String sagaId) {
        return findSagaPort.findBySagaId(sagaId)
                .orElseThrow(() -> new SagaInstanceNotFoundException(sagaId));
    }

    private Optional<SagaStep> findProcessingStep(List<SagaStep> steps) {
        return steps.stream()
                .filter(SagaStep::isProcessing)
                .findFirst();
    }

    private SagaStep findStepByName(List<SagaStep> steps, String stepName) {
        return steps.stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .orElseThrow(() -> new SagaStepNotFoundException(stepName));
    }

    private SagaDefinition<?> getDefinition(String sagaType) {
        SagaDefinition<?> definition = definitionRegistry.get(sagaType);
        if (FormatValidator.hasNoValue(definition)) {
            throw new SagaDefinitionNotFoundException(sagaType);
        }
        return definition;
    }

    private <T> SagaStepDefinition<T> findStepDefinition(SagaDefinition<T> definition, String stepName) {
        return definition.getSteps().stream()
                .filter(step -> step.stepName().equals(stepName))
                .findFirst()
                .orElseThrow(() -> new SagaStepNotFoundException(stepName));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("SAGA 직렬화에 실패했습니다.", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SagaSerializationException("SAGA 역직렬화에 실패했습니다.", e);
        }
    }
}
