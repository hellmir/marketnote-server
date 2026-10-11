package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aligo_message_recipient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoMessageRecipientJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aligo_message_id", nullable = false)
    private AligoMessageJpaEntity aligoMessage;

    @Column(nullable = false, length = 20)
    private String receiver;

    @Column(length = 100)
    private String recvName;

    @Column(length = 100)
    private String msgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AligoMessageRecipientStatus recipientStatus;

    @Column(length = 20)
    private String rslt;

    @Column(length = 500)
    private String rsltMessage;

    private LocalDateTime sentAt;

    private LocalDateTime resultAt;

    private LocalDateTime reportAt;

    public void assignMessage(AligoMessageJpaEntity message) {
        this.aligoMessage = message;
    }

    public void updateResult(AligoMessageRecipientStatus recipientStatus, String msgId, String rslt, String rsltMessage) {
        this.recipientStatus = recipientStatus;
        this.msgId = msgId;
        this.rslt = rslt;
        this.rsltMessage = rsltMessage;
    }

    public static AligoMessageRecipientJpaEntity create(String receiver, String recvName) {
        AligoMessageRecipientJpaEntity entity = new AligoMessageRecipientJpaEntity();
        entity.receiver = receiver;
        entity.recvName = recvName;
        entity.recipientStatus = AligoMessageRecipientStatus.PENDING;
        return entity;
    }
}
