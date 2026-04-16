package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.exception.QuickPaymentTransactionFailedException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPort;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterQuickPaymentTransactionUseCase 테스트")
class RegisterQuickPaymentTransactionUseCaseTest {

    @InjectMocks
    private RegisterQuickPaymentTransactionService registerQuickPaymentTransactionService;

    @Mock
    private RegisterQuickPaymentTransactionPort registerQuickPaymentTransactionPort;

    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("거래 등록 성공")
    class RegisterSuccessTest {

        @Test
        @DisplayName("거래 등록 성공 시 approvalKey, payUrl, traceNo가 반환된다")
        void shouldReturnApprovalKeyAndPayUrlAndTraceNo() {
            RegisterQuickPaymentTransactionCommand command = createCommand();
            RegisterQuickPaymentTransactionPortResult portResult = createSuccessPortResult();

            when(registerQuickPaymentTransactionPort.registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ))).thenReturn(portResult);

            RegisterQuickPaymentTransactionResult result = registerQuickPaymentTransactionService.register(command);

            assertThat(result.approvalKey()).isEqualTo("approval_key_123");
            assertThat(result.payUrl()).isEqualTo("https://testsmpay.kcp.co.kr/pay/mobileGW.kcp");
            assertThat(result.traceNo()).isEqualTo("T0000MGABJUXLY81");
        }

        @Test
        @DisplayName("거래 등록 성공 시 생성된 transactionId가 UUID 형식이다")
        void shouldGenerateTransactionIdInUuidFormat() {
            RegisterQuickPaymentTransactionCommand command = createCommand();
            RegisterQuickPaymentTransactionPortResult portResult = createSuccessPortResult();

            when(registerQuickPaymentTransactionPort.registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ))).thenReturn(portResult);

            RegisterQuickPaymentTransactionResult result = registerQuickPaymentTransactionService.register(command);

            assertThat(result.transactionId()).isNotNull();
            assertThat(isValidUUID(result.transactionId())).isTrue();
            verify(registerQuickPaymentTransactionPort).registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ));
        }
    }

    @Nested
    @DisplayName("거래 등록 실패")
    class RegisterFailureTest {

        @Test
        @DisplayName("KCP 응답 실패 시 QuickPaymentTransactionFailedException이 발생한다")
        void shouldThrowWhenKcpResponseFailed() {
            RegisterQuickPaymentTransactionCommand command = createCommand();
            RegisterQuickPaymentTransactionPortResult portResult = createFailurePortResult();

            when(registerQuickPaymentTransactionPort.registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ))).thenReturn(portResult);

            assertThatThrownBy(() -> registerQuickPaymentTransactionService.register(command))
                    .isInstanceOf(QuickPaymentTransactionFailedException.class);
        }

        @Test
        @DisplayName("KCP 응답 실패 시 resultCode와 resultMessage가 예외 메시지에 포함된다")
        void shouldIncludeResultCodeAndMessageInException() {
            RegisterQuickPaymentTransactionCommand command = createCommand();
            RegisterQuickPaymentTransactionPortResult portResult = createFailurePortResult();

            when(registerQuickPaymentTransactionPort.registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ))).thenReturn(portResult);

            assertThatThrownBy(() -> registerQuickPaymentTransactionService.register(command))
                    .isInstanceOf(QuickPaymentTransactionFailedException.class)
                    .hasMessageContaining("9999")
                    .hasMessageContaining("시스템 오류");
        }

        @Test
        @DisplayName("Port에서 예외 발생 시 그대로 전파된다")
        void shouldPropagatePortException() {
            RegisterQuickPaymentTransactionCommand command = createCommand();

            when(registerQuickPaymentTransactionPort.registerTransaction(argThat(c ->
                    isValidUUID(c.transactionId())
            ))).thenThrow(new RuntimeException("KCP 통신 실패"));

            assertThatThrownBy(() -> registerQuickPaymentTransactionService.register(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("KCP 통신 실패");
        }
    }

    private RegisterQuickPaymentTransactionCommand createCommand() {
        return RegisterQuickPaymentTransactionCommand.builder()
                .userId(USER_ID)
                .build();
    }

    private RegisterQuickPaymentTransactionPortResult createSuccessPortResult() {
        return RegisterQuickPaymentTransactionPortResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("성공")
                .approvalKey("approval_key_123")
                .payUrl("https://testsmpay.kcp.co.kr/pay/mobileGW.kcp")
                .traceNo("T0000MGABJUXLY81")
                .rawResponse("{}")
                .build();
    }

    private RegisterQuickPaymentTransactionPortResult createFailurePortResult() {
        return RegisterQuickPaymentTransactionPortResult.builder()
                .success(false)
                .resultCode("9999")
                .resultMessage("시스템 오류")
                .rawResponse("{}")
                .build();
    }

    private boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
