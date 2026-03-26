package com.personal.marketnote.common.saga.port;

import com.personal.marketnote.common.saga.SagaInstance;
import com.personal.marketnote.common.saga.SagaStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FindSagaPort {

    Optional<SagaInstance> findBySagaId(String sagaId);

    List<SagaStep> findStepsBySagaInstanceId(Long sagaInstanceId);

    List<SagaInstance> findTimedOutProcessingInstances(LocalDateTime cutoff);

    List<SagaInstance> findTimedOutCompensatingInstances(LocalDateTime cutoff);
}
