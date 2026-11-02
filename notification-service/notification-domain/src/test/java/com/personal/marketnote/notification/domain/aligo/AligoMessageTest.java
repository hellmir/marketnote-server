package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoMessageTest {

    @Nested
    @DisplayName("AligoMessage.from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("수신자 1명으로 메시지를 정상 생성한다")
        void shouldCreateMessageWithOneRecipient() {
            // given
            AligoMessageCreateState state = createValidState(1);

            // when
            AligoMessage message = AligoMessage.from(state);

            // then
            assertThat(message.getSenderKey()).isEqualTo("test-sender-key");
            assertThat(message.getTemplateCode()).isEqualTo("TPL001");
            assertThat(message.getSender()).isEqualTo("01012345678");
            assertThat(message.getMessageType()).isEqualTo(AligoMessageType.AT);
            assertThat(message.getContent()).isEqualTo("테스트 메시지");
            assertThat(message.isRequested()).isTrue();
            assertThat(message.getSuccessCount()).isZero();
            assertThat(message.getFailCount()).isZero();
            assertThat(message.getRecipients()).hasSize(1);
        }

        @Test
        @DisplayName("수신자 500명으로 메시지를 정상 생성한다")
        void shouldCreateMessageWith500Recipients() {
            // given
            AligoMessageCreateState state = createValidState(500);

            // when
            AligoMessage message = AligoMessage.from(state);

            // then
            assertThat(message.getRecipients()).hasSize(500);
            assertThat(message.isRequested()).isTrue();
        }

        @Test
        @DisplayName("수신자가 비어있으면 InvalidAligoMessageRecipientCountException을 던진다")
        void shouldThrowExceptionWhenRecipientsEmpty() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender("01012345678")
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(List.of())
                    .build();

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoMessageRecipientCountException.class);
        }

        @Test
        @DisplayName("수신자 null이면 InvalidAligoMessageRecipientCountException을 던진다")
        void shouldThrowExceptionWhenRecipientsNull() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender("01012345678")
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoMessageRecipientCountException.class);
        }

        @Test
        @DisplayName("수신자 501명이면 InvalidAligoMessageRecipientCountException을 던진다")
        void shouldThrowExceptionWhen501Recipients() {
            // given
            AligoMessageCreateState state = createValidState(501);

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoMessageRecipientCountException.class);
        }

        @Test
        @DisplayName("발신번호가 10자리 숫자면 정상 생성된다")
        void shouldCreateMessageWith10DigitSender() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender("0212345678")
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(List.of(createRecipientState("01098765432", "테스트")))
                    .build();

            // when
            AligoMessage message = AligoMessage.from(state);

            // then
            assertThat(message.getSender()).isEqualTo("0212345678");
        }

        @Test
        @DisplayName("발신번호가 숫자가 아니면 InvalidAligoTemplateException을 던진다")
        void shouldThrowExceptionWhenSenderContainsNonDigit() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender("010-1234-5678")
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(List.of(createRecipientState("01098765432", "테스트")))
                    .build();

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoTemplateException.class);
        }

        @Test
        @DisplayName("발신번호가 9자리이면 InvalidAligoTemplateException을 던진다")
        void shouldThrowExceptionWhenSenderTooShort() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender("012345678")
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(List.of(createRecipientState("01098765432", "테스트")))
                    .build();

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoTemplateException.class);
        }

        @Test
        @DisplayName("발신번호가 null이면 InvalidAligoTemplateException을 던진다")
        void shouldThrowExceptionWhenSenderNull() {
            // given
            AligoMessageCreateState state = AligoMessageCreateState.builder()
                    .senderKey("test-sender-key")
                    .templateCode("TPL001")
                    .sender(null)
                    .messageType(AligoMessageType.AT)
                    .content("테스트")
                    .requestedAt(LocalDateTime.now())
                    .recipients(List.of(createRecipientState("01098765432", "테스트")))
                    .build();

            // when & then
            assertThatThrownBy(() -> AligoMessage.from(state))
                    .isInstanceOf(InvalidAligoTemplateException.class);
        }
    }

    @Nested
    @DisplayName("markAsSent")
    class MarkAsSent {

        @Test
        @DisplayName("REQUESTED 상태에서 markAsSent 호출 시 SENT로 전이하고 발송 정보를 갱신한다")
        void shouldTransitionToSentFromRequested() {
            // given
            AligoMessage message = createRequestedMessage();
            LocalDateTime sentAt = LocalDateTime.of(2026, 4, 16, 10, 0, 0);

            // when
            message.markAsSent("MID_001", 5, 1, sentAt);

            // then
            assertThat(message.isSent()).isTrue();
            assertThat(message.getAligoMid()).isEqualTo("MID_001");
            assertThat(message.getSuccessCount()).isEqualTo(5);
            assertThat(message.getFailCount()).isEqualTo(1);
            assertThat(message.getSentAt()).isEqualTo(sentAt);
        }

        @Test
        @DisplayName("SENT 상태에서 markAsSent 호출 시 InvalidAligoMessageStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenMarkAsSentFromSent() {
            // given
            AligoMessage message = createSentMessage();

            // when & then
            assertThatThrownBy(() -> message.markAsSent("MID_002", 1, 0, LocalDateTime.now()))
                    .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);
        }

        @Test
        @DisplayName("FAILED 상태에서 markAsSent 호출 시 InvalidAligoMessageStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenMarkAsSentFromFailed() {
            // given
            AligoMessage message = createFailedMessage();

            // when & then
            assertThatThrownBy(() -> message.markAsSent("MID_003", 1, 0, LocalDateTime.now()))
                    .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("markAsFailed")
    class MarkAsFailed {

        @Test
        @DisplayName("REQUESTED 상태에서 markAsFailed 호출 시 FAILED로 전이한다")
        void shouldTransitionToFailedFromRequested() {
            // given
            AligoMessage message = createRequestedMessage();

            // when
            message.markAsFailed();

            // then
            assertThat(message.isFailed()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED 상태에서 markAsFailed 호출 시 InvalidAligoMessageStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenMarkAsFailedFromCancelled() {
            // given
            AligoMessage message = createCancelledMessage();

            // when & then
            assertThatThrownBy(() -> message.markAsFailed())
                    .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("AligoMessageStatus 상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("REQUESTED에서 SENT, FAILED, CANCELLED, FAILOVERED로 전이할 수 있다")
        void shouldAllowTransitionsFromRequested() {
            assertThat(AligoMessageStatus.REQUESTED.canTransitionTo(AligoMessageStatus.SENT)).isTrue();
            assertThat(AligoMessageStatus.REQUESTED.canTransitionTo(AligoMessageStatus.FAILED)).isTrue();
            assertThat(AligoMessageStatus.REQUESTED.canTransitionTo(AligoMessageStatus.CANCELLED)).isTrue();
            assertThat(AligoMessageStatus.REQUESTED.canTransitionTo(AligoMessageStatus.FAILOVERED)).isTrue();
            assertThat(AligoMessageStatus.REQUESTED.canTransitionTo(AligoMessageStatus.DELIVERED)).isFalse();
        }

        @Test
        @DisplayName("SENT에서 DELIVERED, FAILED로 전이할 수 있다")
        void shouldAllowTransitionsFromSent() {
            assertThat(AligoMessageStatus.SENT.canTransitionTo(AligoMessageStatus.DELIVERED)).isTrue();
            assertThat(AligoMessageStatus.SENT.canTransitionTo(AligoMessageStatus.FAILED)).isTrue();
            assertThat(AligoMessageStatus.SENT.canTransitionTo(AligoMessageStatus.CANCELLED)).isFalse();
            assertThat(AligoMessageStatus.SENT.canTransitionTo(AligoMessageStatus.REQUESTED)).isFalse();
        }

        @Test
        @DisplayName("DELIVERED는 종착 상태로 어디로도 전이할 수 없다")
        void shouldNotAllowTransitionsFromDelivered() {
            for (AligoMessageStatus target : AligoMessageStatus.values()) {
                assertThat(AligoMessageStatus.DELIVERED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("FAILED는 종착 상태로 어디로도 전이할 수 없다")
        void shouldNotAllowTransitionsFromFailed() {
            for (AligoMessageStatus target : AligoMessageStatus.values()) {
                assertThat(AligoMessageStatus.FAILED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("CANCELLED는 종착 상태로 어디로도 전이할 수 없다")
        void shouldNotAllowTransitionsFromCancelled() {
            for (AligoMessageStatus target : AligoMessageStatus.values()) {
                assertThat(AligoMessageStatus.CANCELLED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("FAILOVERED는 종착 상태로 어디로도 전이할 수 없다")
        void shouldNotAllowTransitionsFromFailovered() {
            for (AligoMessageStatus target : AligoMessageStatus.values()) {
                assertThat(AligoMessageStatus.FAILOVERED.canTransitionTo(target)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("markAsFailovered")
    class MarkAsFailovered {

        @Test
        @DisplayName("REQUESTED 상태에서 markAsFailovered 호출 시 FAILOVERED로 전이하고 smid를 recipients에 기록한다")
        void shouldTransitionToFailoveredFromRequested() {
            // given
            AligoMessage message = createRequestedMessageWithRecipients();
            LocalDateTime sentAt = LocalDateTime.of(2026, 4, 16, 10, 0, 0);

            // when
            message.markAsFailovered("MID_001", "SMID_001", 3, 2, sentAt);

            // then
            assertThat(message.isFailovered()).isTrue();
            assertThat(message.getAligoMid()).isEqualTo("MID_001");
            assertThat(message.getSuccessCount()).isEqualTo(3);
            assertThat(message.getFailCount()).isEqualTo(2);
            assertThat(message.getSentAt()).isEqualTo(sentAt);
            assertThat(message.isFailoverUsed()).isTrue();
            assertThat(message.getRecipients()).allSatisfy(recipient ->
                    assertThat(recipient.getSmid()).isEqualTo("SMID_001")
            );
        }

        @Test
        @DisplayName("SENT 상태에서 markAsFailovered 호출 시 InvalidAligoMessageStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenMarkAsFailoveredFromSent() {
            // given
            AligoMessage message = createSentMessage();

            // when & then
            assertThatThrownBy(() -> message.markAsFailovered("MID_002", "SMID_002", 1, 0, LocalDateTime.now()))
                    .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);
        }

        @Test
        @DisplayName("FAILED 상태에서 markAsFailovered 호출 시 InvalidAligoMessageStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenMarkAsFailoveredFromFailed() {
            // given
            AligoMessage message = createFailedMessage();

            // when & then
            assertThatThrownBy(() -> message.markAsFailovered("MID_003", "SMID_003", 1, 0, LocalDateTime.now()))
                    .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("AligoMessageRecipient.updateSmid")
    class UpdateSmid {

        @Test
        @DisplayName("updateSmid 호출 시 smid가 저장된다")
        void shouldStoreSmid() {
            // given
            AligoMessageRecipient recipient = AligoMessageRecipient.from(
                    AligoMessageRecipientCreateState.builder()
                            .receiver("01098765432")
                            .recvName("홍길동")
                            .build()
            );

            // when
            recipient.updateSmid("SMID_TEST_001");

            // then
            assertThat(recipient.getSmid()).isEqualTo("SMID_TEST_001");
        }
    }

    @Nested
    @DisplayName("AligoMessageStatus.isTerminal")
    class IsTerminal {

        @Test
        @DisplayName("DELIVERED, FAILED, CANCELLED, FAILOVERED는 종착 상태이다")
        void shouldReturnTrueForTerminalStatuses() {
            assertThat(AligoMessageStatus.DELIVERED.isTerminal()).isTrue();
            assertThat(AligoMessageStatus.FAILED.isTerminal()).isTrue();
            assertThat(AligoMessageStatus.CANCELLED.isTerminal()).isTrue();
            assertThat(AligoMessageStatus.FAILOVERED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("REQUESTED, SENT는 종착 상태가 아니다")
        void shouldReturnFalseForNonTerminalStatuses() {
            assertThat(AligoMessageStatus.REQUESTED.isTerminal()).isFalse();
            assertThat(AligoMessageStatus.SENT.isTerminal()).isFalse();
        }
    }

    private AligoMessageCreateState createValidState(int recipientCount) {
        List<AligoMessageRecipientCreateState> recipients = IntStream.rangeClosed(1, recipientCount)
                .mapToObj(i -> createRecipientState("010" + String.format("%08d", i), "수신자" + i))
                .toList();

        return AligoMessageCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트 제목")
                .requestedAt(LocalDateTime.now())
                .recipients(recipients)
                .build();
    }

    private AligoMessageRecipientCreateState createRecipientState(String receiver, String recvName) {
        return AligoMessageRecipientCreateState.builder()
                .receiver(receiver)
                .recvName(recvName)
                .build();
    }

    private AligoMessage createRequestedMessageWithRecipients() {
        List<AligoMessageRecipientSnapshotState> recipientStates = List.of(
                AligoMessageRecipientSnapshotState.builder()
                        .id(10L).receiver("01098765432").recvName("수신자1")
                        .recipientStatus(AligoMessageRecipientStatus.PENDING).build(),
                AligoMessageRecipientSnapshotState.builder()
                        .id(11L).receiver("01087654321").recvName("수신자2")
                        .recipientStatus(AligoMessageRecipientStatus.PENDING).build()
        );
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(10L)
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트")
                .title("제목")
                .messageStatus(AligoMessageStatus.REQUESTED)
                .successCount(0)
                .failCount(0)
                .requestedAt(LocalDateTime.now())
                .recipients(recipientStates)
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private AligoMessage createRequestedMessage() {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(1L)
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트")
                .title("제목")
                .messageStatus(AligoMessageStatus.REQUESTED)
                .successCount(0)
                .failCount(0)
                .requestedAt(LocalDateTime.now())
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private AligoMessage createSentMessage() {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(2L)
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트")
                .title("제목")
                .messageStatus(AligoMessageStatus.SENT)
                .aligoMid("MID_EXISTING")
                .successCount(1)
                .failCount(0)
                .requestedAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private AligoMessage createFailedMessage() {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(3L)
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트")
                .title("제목")
                .messageStatus(AligoMessageStatus.FAILED)
                .successCount(0)
                .failCount(0)
                .requestedAt(LocalDateTime.now())
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private AligoMessage createCancelledMessage() {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(4L)
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트")
                .title("제목")
                .messageStatus(AligoMessageStatus.CANCELLED)
                .successCount(0)
                .failCount(0)
                .requestedAt(LocalDateTime.now())
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
