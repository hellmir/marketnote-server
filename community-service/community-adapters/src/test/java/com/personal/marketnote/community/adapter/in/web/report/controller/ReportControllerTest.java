package com.personal.marketnote.community.adapter.in.web.report.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.report.request.RegisterReportRequest;
import com.personal.marketnote.community.adapter.in.web.report.request.UpdateTargetStatusRequest;
import com.personal.marketnote.community.adapter.in.web.report.response.GetReportsResponse;
import com.personal.marketnote.community.adapter.in.web.report.response.UpdateTargetStatusResponse;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.port.in.command.report.ReportCommand;
import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.in.usecase.report.RegisterReportUseCase;
import com.personal.marketnote.community.port.in.usecase.report.UpdateTargetStatusUseCase;
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
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController")
class ReportControllerTest {

    @Mock
    private RegisterReportUseCase registerReportUseCase;

    @Mock
    private GetReportUseCase getReportUseCase;

    @Mock
    private UpdateTargetStatusUseCase updateTargetStatusUseCase;

    @InjectMocks
    private ReportController controller;

    @Nested
    @DisplayName("reportReview")
    class ReportReview {

        @Test
        @DisplayName("인증된 사용자가 리뷰 신고를 요청하면 REVIEW 타입으로 Command를 전달해 UseCase에 위임한다")
        void delegatesReportReview() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(10L);
            RegisterReportRequest request = buildReportRequest("광고성 리뷰입니다.");
            ArgumentCaptor<ReportCommand> captor = ArgumentCaptor.forClass(ReportCommand.class);

            ResponseEntity<BaseResponse<Void>> response = controller.reportReview(100L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerReportUseCase).report(captor.capture());
            ReportCommand command = captor.getValue();
            assertThat(command.targetType()).isEqualTo(ReportTargetType.REVIEW);
            assertThat(command.targetId()).isEqualTo(100L);
            assertThat(command.reporterId()).isEqualTo(10L);
            assertThat(command.reason()).isEqualTo("광고성 리뷰입니다.");
        }
    }

    @Nested
    @DisplayName("reportProductInqueryPost")
    class ReportPost {

        @Test
        @DisplayName("인증된 사용자가 게시글 신고를 요청하면 POST 타입으로 Command를 전달해 UseCase에 위임한다")
        void delegatesReportPost() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(20L);
            RegisterReportRequest request = buildReportRequest("욕설");
            ArgumentCaptor<ReportCommand> captor = ArgumentCaptor.forClass(ReportCommand.class);

            ResponseEntity<BaseResponse<Void>> response =
                    controller.reportProductInqueryPost(200L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerReportUseCase).report(captor.capture());
            ReportCommand command = captor.getValue();
            assertThat(command.targetType()).isEqualTo(ReportTargetType.POST);
            assertThat(command.targetId()).isEqualTo(200L);
            assertThat(command.reporterId()).isEqualTo(20L);
            assertThat(command.reason()).isEqualTo("욕설");
        }
    }

    @Nested
    @DisplayName("getReviewReports")
    class GetReviewReports {

        @Test
        @DisplayName("리뷰 ID로 신고 내역을 조회해 OK 응답을 반환한다")
        void returnsReviewReports() {
            Report sampleReport = Report.of(ReportTargetType.REVIEW, 100L, 10L, "광고성");
            LocalDateTime now = LocalDateTime.now();
            ReflectionTestUtils.setField(sampleReport, "createdAt", now);
            when(getReportUseCase.getReports(ReportTargetType.REVIEW, 100L))
                    .thenReturn(List.of(sampleReport));

            ResponseEntity<BaseResponse<GetReportsResponse>> response = controller.getReviewReports(100L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getPostReports")
    class GetPostReports {

        @Test
        @DisplayName("게시글 ID로 신고 내역을 조회해 OK 응답을 반환한다")
        void returnsPostReports() {
            when(getReportUseCase.getReports(ReportTargetType.POST, 200L))
                    .thenReturn(List.of());

            ResponseEntity<BaseResponse<GetReportsResponse>> response = controller.getPostReports(200L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateTargetStatus")
    class UpdateTargetStatus {

        @Test
        @DisplayName("isVisible=true이면 노출 상태로 전환하고 응답 body에 true를 포함한다")
        void updatesToVisibleAndEchoesState() {
            UpdateTargetStatusRequest request = new UpdateTargetStatusRequest(
                    ReportTargetType.REVIEW, 100L, true
            );

            ResponseEntity<BaseResponse<UpdateTargetStatusResponse>> response =
                    controller.updateTargetStatus(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isVisible()).isTrue();
            verify(updateTargetStatusUseCase).updateTargetStatus(any(UpdateTargetStatusCommand.class));
        }

        @Test
        @DisplayName("isVisible=false이면 숨기기 상태로 전환하고 응답 body에 false를 포함한다")
        void updatesToHiddenAndEchoesState() {
            UpdateTargetStatusRequest request = new UpdateTargetStatusRequest(
                    ReportTargetType.POST, 200L, false
            );

            ResponseEntity<BaseResponse<UpdateTargetStatusResponse>> response =
                    controller.updateTargetStatus(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().isVisible()).isFalse();
            verify(updateTargetStatusUseCase).updateTargetStatus(any(UpdateTargetStatusCommand.class));
        }
    }

    private RegisterReportRequest buildReportRequest(String reason) {
        RegisterReportRequest request = new RegisterReportRequest();
        ReflectionTestUtils.setField(request, "reason", reason);
        return request;
    }

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    private static <T> org.mockito.stubbing.OngoingStubbing<T> when(T call) {
        return org.mockito.Mockito.when(call);
    }

    private static <T> T any(Class<T> type) {
        return org.mockito.ArgumentMatchers.any(type);
    }
}
