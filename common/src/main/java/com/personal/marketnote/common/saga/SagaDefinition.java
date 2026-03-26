package com.personal.marketnote.common.saga;

import java.util.List;

public interface SagaDefinition<T> {

    String getSagaType();

    Class<T> getContextType();

    List<SagaStepDefinition<T>> getSteps();
}
