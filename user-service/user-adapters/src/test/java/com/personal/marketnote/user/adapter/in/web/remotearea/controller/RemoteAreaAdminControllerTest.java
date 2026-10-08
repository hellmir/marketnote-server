package com.personal.marketnote.user.adapter.in.web.remotearea.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.remotearea.request.RegisterRemoteAreaRequest;
import com.personal.marketnote.user.adapter.in.web.remotearea.response.GetRemoteAreaResponse;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;
import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaItemResult;
import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaResult;
import com.personal.marketnote.user.port.in.usecase.remotearea.DeleteRemoteAreaUseCase;
import com.personal.marketnote.user.port.in.usecase.remotearea.GetRemoteAreaUseCase;
import com.personal.marketnote.user.port.in.usecase.remotearea.RegisterRemoteAreaUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoteAreaAdminController 도서산간 관리")
class RemoteAreaAdminControllerTest {

    @InjectMocks
    private RemoteAreaAdminController controller;

    @Mock
    private RegisterRemoteAreaUseCase registerRemoteAreaUseCase;

    @Mock
    private GetRemoteAreaUseCase getRemoteAreaUseCase;

    @Mock
    private DeleteRemoteAreaUseCase deleteRemoteAreaUseCase;

    @Nested
    @DisplayName("POST /api/v1/admin/remote-areas - 도서산간 등록")
    class RegisterRemoteArea {

        @Test
        @DisplayName("도서산간 등록 요청 필드를 Command로 매핑하여 위임하고 CREATED를 반환한다")
        void registersRemoteArea() {
            // given
            RegisterRemoteAreaRequest request = new RegisterRemoteAreaRequest();
            ReflectionTestUtils.setField(request, "province", "제주특별자치도");
            ReflectionTestUtils.setField(request, "district", "서귀포시");
            ReflectionTestUtils.setField(request, "village", "대정읍");
            ReflectionTestUtils.setField(request, "subarea", "마라도");
            ReflectionTestUtils.setField(request, "regionType", "ISLAND");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.registerRemoteArea(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterRemoteAreaCommand> captor =
                    ArgumentCaptor.forClass(RegisterRemoteAreaCommand.class);
            verify(registerRemoteAreaUseCase).registerRemoteArea(captor.capture());
            RegisterRemoteAreaCommand captured = captor.getValue();
            assertThat(captured.province()).isEqualTo("제주특별자치도");
            assertThat(captured.district()).isEqualTo("서귀포시");
            assertThat(captured.village()).isEqualTo("대정읍");
            assertThat(captured.subarea()).isEqualTo("마라도");
            assertThat(captured.regionType()).isEqualTo("ISLAND");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/remote-areas - 도서산간 목록 조회")
    class GetRemoteAreas {

        @Test
        @DisplayName("도서산간 지역 목록을 응답으로 변환하여 OK를 반환한다")
        void returnsRemoteAreaList() {
            // given
            GetRemoteAreaResult result = new GetRemoteAreaResult(List.of(
                    new GetRemoteAreaItemResult(1L, "제주특별자치도", "서귀포시", "대정읍", "마라도")
            ));
            when(getRemoteAreaUseCase.getRemoteAreas()).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetRemoteAreaResponse>> response = controller.getRemoteAreas();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getRemoteAreaUseCase).getRemoteAreas();
            verifyNoInteractions(registerRemoteAreaUseCase, deleteRemoteAreaUseCase);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/remote-areas/{id} - 도서산간 삭제")
    class DeleteRemoteArea {

        @Test
        @DisplayName("도서산간 삭제 시 ID를 UseCase에 위임한다")
        void deletesRemoteAreaById() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.deleteRemoteArea(99L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteRemoteAreaUseCase).deleteRemoteArea(99L);
            verifyNoInteractions(registerRemoteAreaUseCase, getRemoteAreaUseCase);
        }
    }
}
