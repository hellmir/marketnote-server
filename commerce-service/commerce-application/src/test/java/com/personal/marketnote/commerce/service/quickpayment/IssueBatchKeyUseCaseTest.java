package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.exception.QuickPaymentBatchKeyIssuanceFailedException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPort;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.SaveQuickPaymentCardPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IssueBatchKeyUseCase 테스트")
class IssueBatchKeyUseCaseTest {

    @InjectMocks
    private IssueBatchKeyService issueBatchKeyService;

    @Mock
    private IssueBatchKeyPort issueBatchKeyPort;

    @Mock
    private SaveQuickPaymentCardPort saveQuickPaymentCardPort;

    private static final Long USER_ID = 1L;
    private static final Long SAVED_CARD_ID = 100L;

    @Nested
    @DisplayName("배치키 발급 성공")
    class IssueBatchKeySuccessTest {

        @Test
        @DisplayName("배치키 발급 성공 시 QuickPaymentCard가 DB에 저장된다")
        void shouldSaveQuickPaymentCardToDb() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createSuccessPortResult();
            QuickPaymentCard savedCard = createSavedCard();

            when(issueBatchKeyPort.issueBatchKey(argThat(c ->
                    "test_enc_data".equals(c.encData()) && "test_enc_info".equals(c.encInfo())
            ))).thenReturn(portResult);
            when(saveQuickPaymentCardPort.save(any(QuickPaymentCard.class))).thenReturn(savedCard);

            issueBatchKeyService.issueBatchKey(command);

            verify(saveQuickPaymentCardPort).save(any(QuickPaymentCard.class));
        }

        @Test
        @DisplayName("배치키 발급 성공 시 카드 정보가 결과에 포함된다")
        void shouldReturnCardInfoInResult() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createSuccessPortResult();
            QuickPaymentCard savedCard = createSavedCard();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);
            when(saveQuickPaymentCardPort.save(any(QuickPaymentCard.class))).thenReturn(savedCard);

            IssueBatchKeyResult result = issueBatchKeyService.issueBatchKey(command);

            assertThat(result.cardCode()).isEqualTo("CCDI");
            assertThat(result.cardName()).isEqualTo("현대카드");
            assertThat(result.cardBinType01()).isEqualTo("0");
            assertThat(result.cardBinType02()).isEqualTo("0");
        }

        @Test
        @DisplayName("배치키 발급 성공 시 SaveQuickPaymentCardPort.save()가 1회 호출된다")
        void shouldCallSavePortOnce() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createSuccessPortResult();
            QuickPaymentCard savedCard = createSavedCard();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);
            when(saveQuickPaymentCardPort.save(any(QuickPaymentCard.class))).thenReturn(savedCard);

            issueBatchKeyService.issueBatchKey(command);

            verify(saveQuickPaymentCardPort, times(1)).save(any(QuickPaymentCard.class));
        }

        @Test
        @DisplayName("배치키 발급 성공 시 저장된 QuickPaymentCard의 userId가 command의 userId와 일치한다")
        void shouldSaveCardWithCorrectUserId() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createSuccessPortResult();
            QuickPaymentCard savedCard = createSavedCard();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);
            when(saveQuickPaymentCardPort.save(argThat(card ->
                    USER_ID.equals(card.getUserId())
            ))).thenReturn(savedCard);

            IssueBatchKeyResult result = issueBatchKeyService.issueBatchKey(command);

            assertThat(result.quickPaymentCardId()).isEqualTo(SAVED_CARD_ID);
            verify(saveQuickPaymentCardPort).save(argThat(card ->
                    USER_ID.equals(card.getUserId())
                            && "batch_key_123".equals(card.getBatchKey())
                            && "CCDI".equals(card.getCardCode())
            ));
        }
    }

    @Nested
    @DisplayName("배치키 발급 실패")
    class IssueBatchKeyFailureTest {

        @Test
        @DisplayName("KCP 응답 실패 시 QuickPaymentBatchKeyIssuanceFailedException이 발생한다")
        void shouldThrowWhenKcpResponseFailed() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createFailurePortResult();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);

            assertThatThrownBy(() -> issueBatchKeyService.issueBatchKey(command))
                    .isInstanceOf(QuickPaymentBatchKeyIssuanceFailedException.class);
        }

        @Test
        @DisplayName("KCP 응답 실패 시 resultCode와 resultMessage가 예외 메시지에 포함된다")
        void shouldIncludeResultCodeAndMessageInException() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createFailurePortResult();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);

            assertThatThrownBy(() -> issueBatchKeyService.issueBatchKey(command))
                    .isInstanceOf(QuickPaymentBatchKeyIssuanceFailedException.class)
                    .hasMessageContaining("8102")
                    .hasMessageContaining("인증 데이터 오류");
        }

        @Test
        @DisplayName("KCP 응답 실패 시 SaveQuickPaymentCardPort.save()가 호출되지 않는다")
        void shouldNotCallSavePortWhenFailed() {
            IssueBatchKeyCommand command = createCommand();
            IssueBatchKeyPortResult portResult = createFailurePortResult();

            when(issueBatchKeyPort.issueBatchKey(any())).thenReturn(portResult);

            assertThatThrownBy(() -> issueBatchKeyService.issueBatchKey(command))
                    .isInstanceOf(QuickPaymentBatchKeyIssuanceFailedException.class);

            verify(saveQuickPaymentCardPort, never()).save(any());
        }

        @Test
        @DisplayName("IssueBatchKeyPort에서 예외 발생 시 그대로 전파된다")
        void shouldPropagatePortException() {
            IssueBatchKeyCommand command = createCommand();

            when(issueBatchKeyPort.issueBatchKey(any()))
                    .thenThrow(new RuntimeException("KCP 통신 실패"));

            assertThatThrownBy(() -> issueBatchKeyService.issueBatchKey(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("KCP 통신 실패");

            verify(saveQuickPaymentCardPort, never()).save(any());
        }
    }

    private IssueBatchKeyCommand createCommand() {
        return IssueBatchKeyCommand.builder()
                .userId(USER_ID)
                .encData("test_enc_data")
                .encInfo("test_enc_info")
                .build();
    }

    private IssueBatchKeyPortResult createSuccessPortResult() {
        return IssueBatchKeyPortResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("성공")
                .batchKey("batch_key_123")
                .cardCode("CCDI")
                .cardName("현대카드")
                .cardBinType01("0")
                .cardBinType02("0")
                .rawResponse("{}")
                .build();
    }

    private IssueBatchKeyPortResult createFailurePortResult() {
        return IssueBatchKeyPortResult.builder()
                .success(false)
                .resultCode("8102")
                .resultMessage("인증 데이터 오류")
                .rawResponse("{}")
                .build();
    }

    private QuickPaymentCard createSavedCard() {
        QuickPaymentCard card = QuickPaymentCard.from(
                com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardSnapshotState.builder()
                        .id(SAVED_CARD_ID)
                        .userId(USER_ID)
                        .batchKey("batch_key_123")
                        .cardCode("CCDI")
                        .cardName("현대카드")
                        .cardBinType01("0")
                        .cardBinType02("0")
                        .status(com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE)
                        .build()
        );
        return card;
    }
}
