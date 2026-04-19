package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardSnapshotState;
import com.personal.marketnote.commerce.exception.QuickPaymentBatchKeyDeletionFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentCardNotFoundException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.DeleteQuickPaymentCardCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.DeleteBatchKeyPort;
import com.personal.marketnote.commerce.port.out.quickpayment.DeleteBatchKeyPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.DeleteQuickPaymentCardPort;
import com.personal.marketnote.commerce.port.out.quickpayment.FindQuickPaymentCardPort;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteQuickPaymentCardUseCase 테스트")
class DeleteQuickPaymentCardUseCaseTest {

    @InjectMocks
    private DeleteQuickPaymentCardService deleteQuickPaymentCardService;

    @Mock
    private FindQuickPaymentCardPort findQuickPaymentCardPort;

    @Mock
    private DeleteBatchKeyPort deleteBatchKeyPort;

    @Mock
    private DeleteQuickPaymentCardPort deleteQuickPaymentCardPort;

    private static final Long USER_ID = 1L;
    private static final Long CARD_ID = 10L;

    @Nested
    @DisplayName("삭제 성공")
    class DeleteSuccessTest {

        @Test
        @DisplayName("KCP 배치키 삭제 성공 시 카드가 비활성화된다")
        void shouldDeactivateCardWhenKcpDeletionSucceeds() {
            DeleteQuickPaymentCardCommand command = createCommand();
            QuickPaymentCard card = createCard();
            DeleteBatchKeyPortResult portResult = createSuccessPortResult();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(card));
            when(deleteBatchKeyPort.deleteBatchKey(argThat(c ->
                    "batch_key_test".equals(c.batchKey()) && "group_test".equals(c.groupId())
            ))).thenReturn(portResult);

            deleteQuickPaymentCardService.delete(command);

            verify(deleteQuickPaymentCardPort).deactivate(argThat(c ->
                    c.isInactive()
            ));
        }

        @Test
        @DisplayName("KCP 배치키 삭제 성공 시 DeleteBatchKeyPort가 1회 호출된다")
        void shouldCallDeleteBatchKeyPortOnce() {
            DeleteQuickPaymentCardCommand command = createCommand();
            QuickPaymentCard card = createCard();
            DeleteBatchKeyPortResult portResult = createSuccessPortResult();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(card));
            when(deleteBatchKeyPort.deleteBatchKey(any())).thenReturn(portResult);

            deleteQuickPaymentCardService.delete(command);

            verify(deleteBatchKeyPort, times(1)).deleteBatchKey(any());
            verify(deleteQuickPaymentCardPort, times(1)).deactivate(any());
        }
    }

    @Nested
    @DisplayName("삭제 실패")
    class DeleteFailureTest {

        @Test
        @DisplayName("빠른결제 카드가 존재하지 않으면 QuickPaymentCardNotFoundException이 발생한다")
        void shouldThrowWhenCardNotFound() {
            DeleteQuickPaymentCardCommand command = createCommand();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> deleteQuickPaymentCardService.delete(command))
                    .isInstanceOf(QuickPaymentCardNotFoundException.class);

            verify(deleteBatchKeyPort, never()).deleteBatchKey(any());
            verify(deleteQuickPaymentCardPort, never()).deactivate(any());
        }

        @Test
        @DisplayName("KCP 배치키 삭제 실패 시 QuickPaymentBatchKeyDeletionFailedException이 발생한다")
        void shouldThrowWhenKcpDeletionFails() {
            DeleteQuickPaymentCardCommand command = createCommand();
            QuickPaymentCard card = createCard();
            DeleteBatchKeyPortResult portResult = DeleteBatchKeyPortResult.builder()
                    .success(false)
                    .resultCode("9999")
                    .resultMessage("시스템 오류")
                    .build();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(card));
            when(deleteBatchKeyPort.deleteBatchKey(any())).thenReturn(portResult);

            assertThatThrownBy(() -> deleteQuickPaymentCardService.delete(command))
                    .isInstanceOf(QuickPaymentBatchKeyDeletionFailedException.class)
                    .hasMessageContaining("9999")
                    .hasMessageContaining("시스템 오류");

            verify(deleteQuickPaymentCardPort, never()).deactivate(any());
        }

        @Test
        @DisplayName("KCP 통신 중 예외 발생 시 그대로 전파된다")
        void shouldPropagatePortException() {
            DeleteQuickPaymentCardCommand command = createCommand();
            QuickPaymentCard card = createCard();

            when(findQuickPaymentCardPort.findActiveByIdAndUserId(CARD_ID, USER_ID))
                    .thenReturn(Optional.of(card));
            when(deleteBatchKeyPort.deleteBatchKey(any()))
                    .thenThrow(new RuntimeException("KCP 통신 실패"));

            assertThatThrownBy(() -> deleteQuickPaymentCardService.delete(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("KCP 통신 실패");

            verify(deleteQuickPaymentCardPort, never()).deactivate(any());
        }
    }

    private DeleteQuickPaymentCardCommand createCommand() {
        return DeleteQuickPaymentCardCommand.builder()
                .quickPaymentCardId(CARD_ID)
                .userId(USER_ID)
                .build();
    }

    private QuickPaymentCard createCard() {
        return QuickPaymentCard.from(QuickPaymentCardSnapshotState.builder()
                .id(CARD_ID)
                .userId(USER_ID)
                .batchKey("batch_key_test")
                .groupId("group_test")
                .cardCode("CCDI")
                .cardName("현대카드")
                .cardBinType01("0")
                .cardBinType02("0")
                .status(EntityStatus.ACTIVE)
                .build());
    }

    private DeleteBatchKeyPortResult createSuccessPortResult() {
        return DeleteBatchKeyPortResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("성공")
                .build();
    }
}
