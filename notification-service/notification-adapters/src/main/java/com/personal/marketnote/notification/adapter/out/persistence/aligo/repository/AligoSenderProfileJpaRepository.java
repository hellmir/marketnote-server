package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoSenderProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AligoSenderProfileJpaRepository extends JpaRepository<AligoSenderProfileJpaEntity, Long> {

    Optional<AligoSenderProfileJpaEntity> findBySenderKey(String senderKey);
}
