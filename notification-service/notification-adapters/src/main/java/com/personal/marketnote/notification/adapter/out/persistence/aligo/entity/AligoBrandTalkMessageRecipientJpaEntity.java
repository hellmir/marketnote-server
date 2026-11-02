package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aligo_brand_talk_message_recipient")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoBrandTalkMessageRecipientJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_talk_message_id", nullable = false)
    private AligoBrandTalkMessageJpaEntity brandTalkMessage;

    @Column(nullable = false, length = 20)
    private String receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String receiverMessage;

    @Column(length = 100)
    private String msgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AligoMessageRecipientStatus recipientStatus;

    @Column(length = 20)
    private String rslt;

    @Column(length = 500)
    private String rsltMessage;

    public static AligoBrandTalkMessageRecipientJpaEntity create(String receiver, String receiverMessage) {
        AligoBrandTalkMessageRecipientJpaEntity entity = new AligoBrandTalkMessageRecipientJpaEntity();
        entity.receiver = receiver;
        entity.receiverMessage = receiverMessage;
        entity.recipientStatus = AligoMessageRecipientStatus.PENDING;
        return entity;
    }

    public void assignMessage(AligoBrandTalkMessageJpaEntity message) {
        this.brandTalkMessage = message;
    }
}
