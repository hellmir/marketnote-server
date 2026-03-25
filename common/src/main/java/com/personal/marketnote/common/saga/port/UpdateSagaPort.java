package com.personal.marketnote.common.saga.port;

import com.personal.marketnote.common.saga.SagaInstance;
import com.personal.marketnote.common.saga.SagaStep;

public interface UpdateSagaPort {

    void update(SagaInstance instance);

    void updateStep(SagaStep step);
}
