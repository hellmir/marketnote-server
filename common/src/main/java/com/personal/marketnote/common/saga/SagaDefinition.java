package com.personal.marketnote.common.saga;

import java.util.List;

public interface SagaDefinition<T> {

    String getSagaType();

    List<SagaStepDefinition<T>> getSteps();
}
