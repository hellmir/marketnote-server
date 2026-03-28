package com.personal.marketnote.reward.adapter.in.web.point;

import com.personal.marketnote.reward.adapter.in.web.point.mapper.PointRequestToCommandMapper;
import com.personal.marketnote.reward.adapter.in.web.point.request.CancelPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ConfirmPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyUserPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointByIdResponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.UpdateUserPointResponse;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointResult;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalPointController 테스트")
class InternalPointControllerTest {
    @InjectMocks
    private InternalPointController internalPointController;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;

    @Mock
    private ConfirmPendingPointUseCase confirmPendingPointUseCase;

    @Mock
    private CancelPendingPointUseCase cancelPendingPointUseCase;

    private static final Long USER_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 18, 10, 0, 0);

    @Nested
    @DisplayName("getUserPoint")
    class GetUserPoint {
        @Test
        @DisplayName("회원 포인트 정보를 조회하고 200 OK를 반환한다")
        void shouldReturnUserPointAndOkStatus() {
            // given
            UserPoint userPoint = mock(UserPoint.class);
            when(userPoint.getUserId()).thenReturn(USER_ID);
            when(userPoint.getAmountValue()).thenReturn(5000L);
            when(userPoint.getAddExpectedAmount()).thenReturn(1000L);
            when(userPoint.getExpireExpectedAmount()).thenReturn(0L);
            when(userPoint.getCreatedAt()).thenReturn(NOW);
            when(userPoint.getModifiedAt()).thenReturn(NOW);
            when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(userPoint);

            // when
            ResponseEntity<?> response = internalPointController.getUserPoint(USER_ID);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getUserPointUseCase).getUserPoint(USER_ID);
        }
    }

    @Nested
    @DisplayName("modifyUserPoint")
    class ModifyUserPoint {
        @Test
        @DisplayName("회원 포인트를 수정하고 200 OK를 반환한다")
        void shouldModifyUserPointAndReturnOkStatus() {
            // given
            ModifyUserPointRequest request = new ModifyUserPointRequest();
            request.setChangeType(UserPointChangeType.ACCRUAL);
            request.setAmount(1000L);
            request.setSourceType(UserPointSourceType.ORDER);
            request.setSourceId(100L);
            request.setReason("주문 포인트 적립");

            UpdateUserPointResult result = new UpdateUserPointResult(
                    USER_ID, 6000L, 1000L, 0L, NOW, NOW
            );
            when(modifyUserPointUseCase.modify(any(ModifyUserPointCommand.class))).thenReturn(result);

            // when
            ResponseEntity<?> response = internalPointController.modifyUserPoint(USER_ID, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        }
    }

    @Nested
    @DisplayName("modifyPendingPoint")
    class ModifyPendingPoint {
        @Test
        @DisplayName("적립 예정 포인트를 수정하고 200 OK를 반환한다")
        void shouldModifyPendingPointAndReturnOkStatus() {
            // given
            ModifyPendingPointRequest request = new ModifyPendingPointRequest();
            request.setChangeType(UserPointChangeType.ACCRUAL);
            request.setAmount(500L);
            request.setSourceType(UserPointSourceType.ORDER);
            request.setSourceId(200L);
            request.setReason("상품 구매 적립");

            UpdateUserPointResult result = new UpdateUserPointResult(
                    USER_ID, 5000L, 1500L, 0L, NOW, NOW
            );
            when(modifyPendingPointUseCase.modifyPending(any(ModifyPendingPointCommand.class))).thenReturn(result);

            // when
            ResponseEntity<?> response = internalPointController.modifyPendingPoint(USER_ID, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(modifyPendingPointUseCase).modifyPending(any(ModifyPendingPointCommand.class));
        }
    }

    @Nested
    @DisplayName("confirmPendingPoint")
    class ConfirmPendingPoint {
        @Test
        @DisplayName("적립 예정 포인트를 확정하고 200 OK를 반환한다")
        void shouldConfirmPendingPointAndReturnOkStatus() {
            // given
            ConfirmPendingPointRequest request = new ConfirmPendingPointRequest();
            request.setSourceType(UserPointSourceType.ORDER);
            request.setSourceId(300L);
            request.setReason("구매 확정 포인트 적립");

            UpdateUserPointResult result = new UpdateUserPointResult(
                    USER_ID, 6500L, 500L, 0L, NOW, NOW
            );
            when(confirmPendingPointUseCase.confirmPending(any(ConfirmPendingPointCommand.class))).thenReturn(result);

            // when
            ResponseEntity<?> response = internalPointController.confirmPendingPoint(USER_ID, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(confirmPendingPointUseCase).confirmPending(any(ConfirmPendingPointCommand.class));
        }
    }

    @Nested
    @DisplayName("cancelPendingPoint")
    class CancelPendingPoint {
        @Test
        @DisplayName("적립 예정 포인트를 취소하고 200 OK를 반환한다")
        void shouldCancelPendingPointAndReturnOkStatus() {
            // given
            CancelPendingPointRequest request = new CancelPendingPointRequest();
            request.setSourceType(UserPointSourceType.ORDER);
            request.setSourceId(400L);
            request.setReason("결제 취소 적립 예정 포인트 회수");

            UpdateUserPointResult result = new UpdateUserPointResult(
                    USER_ID, 5000L, 0L, 0L, NOW, NOW
            );
            when(cancelPendingPointUseCase.cancelPending(any(CancelPendingPointCommand.class))).thenReturn(result);

            // when
            ResponseEntity<?> response = internalPointController.cancelPendingPoint(USER_ID, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelPendingPointUseCase).cancelPending(any(CancelPendingPointCommand.class));
        }
    }
}
