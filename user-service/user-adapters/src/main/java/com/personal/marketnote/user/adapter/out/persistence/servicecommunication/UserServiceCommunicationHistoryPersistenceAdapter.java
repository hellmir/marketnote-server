package com.personal.marketnote.user.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.user.adapter.out.persistence.servicecommunication.entity.UserServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.servicecommunication.repository.UserServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;
import com.personal.marketnote.user.port.out.servicecommunication.SaveUserServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserServiceCommunicationHistoryPersistenceAdapter
        implements SaveUserServiceCommunicationHistoryPort {
    private final UserServiceCommunicationHistoryJpaRepository repository;

    @Override
    public UserServiceCommunicationHistory save(UserServiceCommunicationHistory history) {
        UserServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                UserServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
