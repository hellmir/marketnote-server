package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.RegisterSettlementPolicyRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.UpdateSettlementPolicyRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementPolicyResponse;
import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.port.in.command.settlement.RegisterSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.command.settlement.UpdateSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.DeleteSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.RegisterSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.UpdateSettlementPolicyUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.EntityStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementPolicyController 테스트")
class SettlementPolicyControllerTest {

    @InjectMocks
    private SettlementPolicyController settlementPolicyController;

    @Mock
    private RegisterSettlementPolicyUseCase registerSettlementPolicyUseCase;

    @Mock
    private GetSettlementPolicyUseCase getSettlementPolicyUseCase;

    @Mock
    private UpdateSettlementPolicyUseCase updateSettlementPolicyUseCase;

    @Mock
    private DeleteSettlementPolicyUseCase deleteSettlementPolicyUseCase;

    private GetSettlementPolicyResult samplePolicyResult() {
        return GetSettlementPolicyResult.builder()
                .id(1L)
                .sellerId(100L)
                .pgFeeRate(30)
                .platformFeeRate(50)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10_000L)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .modifiedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("registerPolicy")
    class RegisterPolicy {

        @Test
        @DisplayName("정산 정책 등록이 성공하면 200 OK와 등록된 정책을 반환한다")
        void shouldReturnOkWhenRegisterSucceeds() {
            // given
            RegisterSettlementPolicyRequest request = mock(RegisterSettlementPolicyRequest.class);
            when(request.getSellerId()).thenReturn(100L);
            when(request.getPgFeeRate()).thenReturn(30);
            when(request.getPlatformFeeRate()).thenReturn(50);
            when(request.getSettlementCycle()).thenReturn("MONTHLY");
            when(request.getMinPayoutAmount()).thenReturn(10_000L);

            when(registerSettlementPolicyUseCase.registerPolicy(any(RegisterSettlementPolicyCommand.class)))
                    .thenReturn(samplePolicyResult());

            // when
            ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> response =
                    settlementPolicyController.registerPolicy(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            verify(registerSettlementPolicyUseCase).registerPolicy(any(RegisterSettlementPolicyCommand.class));
        }
    }

    @Nested
    @DisplayName("getAllPolicies")
    class GetAllPolicies {

        @Test
        @DisplayName("정산 정책 전체 조회가 성공하면 200 OK를 반환한다")
        void shouldReturnAllPolicies() {
            // given
            when(getSettlementPolicyUseCase.getAllPolicies()).thenReturn(List.of(samplePolicyResult()));

            // when
            ResponseEntity<BaseResponse<List<GetSettlementPolicyResponse>>> response =
                    settlementPolicyController.getAllPolicies();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            verify(getSettlementPolicyUseCase).getAllPolicies();
        }
    }

    @Nested
    @DisplayName("getPolicy")
    class GetPolicy {

        @Test
        @DisplayName("정산 정책 단건 조회가 성공하면 200 OK를 반환한다")
        void shouldReturnPolicy() {
            // given
            when(getSettlementPolicyUseCase.getPolicy(1L)).thenReturn(samplePolicyResult());

            // when
            ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> response =
                    settlementPolicyController.getPolicy(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            verify(getSettlementPolicyUseCase).getPolicy(1L);
        }
    }

    @Nested
    @DisplayName("updatePolicy")
    class UpdatePolicy {

        @Test
        @DisplayName("정산 정책 수정이 성공하면 200 OK를 반환한다")
        void shouldReturnUpdatedPolicy() {
            // given
            UpdateSettlementPolicyRequest request = mock(UpdateSettlementPolicyRequest.class);
            when(request.getPgFeeRate()).thenReturn(25);
            when(request.getPlatformFeeRate()).thenReturn(45);
            when(request.getSettlementCycle()).thenReturn("WEEKLY");
            when(request.getMinPayoutAmount()).thenReturn(5_000L);
            when(updateSettlementPolicyUseCase.updatePolicy(any(UpdateSettlementPolicyCommand.class)))
                    .thenReturn(samplePolicyResult());

            // when
            ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> response =
                    settlementPolicyController.updatePolicy(1L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateSettlementPolicyUseCase).updatePolicy(any(UpdateSettlementPolicyCommand.class));
        }
    }

    @Nested
    @DisplayName("deletePolicy")
    class DeletePolicy {

        @Test
        @DisplayName("정산 정책 삭제가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenDeleteSucceeds() {
            // when
            ResponseEntity<BaseResponse<Void>> response =
                    settlementPolicyController.deletePolicy(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteSettlementPolicyUseCase).deletePolicy(1L);
        }
    }
}
