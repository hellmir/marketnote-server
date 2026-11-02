package com.personal.marketnote.common.saga.port;

import com.personal.marketnote.common.saga.SagaInstance;
import com.personal.marketnote.common.saga.SagaStep;

public interface SaveSagaPort {

    SagaInstance save(SagaInstance instance);

    SagaStep saveStep(SagaStep step);
}
