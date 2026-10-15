package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;
import com.personal.marketnote.notification.port.out.aligo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBrandTalkTemplateListUseCaseTest {

    @InjectMocks
    private GetBrandTalkTemplateListService getBrandTalkTemplateListService;

    @Mock
    private GetBrandTalkTemplateListExternalPort getTemplateListExternalPort;

    @Mock
    private FindBrandTalkTemplateListPort findBrandTalkTemplateListPort;

    @Mock
    private SaveBrandTalkTemplatePort saveBrandTalkTemplatePort;

    @Mock
    private UpdateBrandTalkTemplatePort updateBrandTalkTemplatePort;

    private static final String SENDER_KEY = "test-sender-key";

    @Test
    @DisplayName("외부 API 호출 후 결과를 반환한다")
    void shouldReturnExternalApiResult() {
        // given
        GetBrandTalkTemplateListResult.ExternalListResponse externalResponse =
                new GetBrandTalkTemplateListResult.ExternalListResponse(
                        new GetBrandTalkTemplateListResult.PageInfo(1, 10, 1),
                        List.of(new GetBrandTalkTemplateListResult.ExternalTemplateItem(
                                "TPL001", "주문 안내", "TEXT", "O", List.of("#{name}"), "2026-10-11 12:00:00"
                        ))
                );
        when(getTemplateListExternalPort.getTemplateList(SENDER_KEY, 1, 10)).thenReturn(externalResponse);

        AligoBrandTalkTemplate localTemplate = createTemplate("TPL001", BrandTalkTemplateStatus.NORMAL);
        when(findBrandTalkTemplateListPort.findAllBySenderKey(SENDER_KEY)).thenReturn(List.of(localTemplate));

        // when
        GetBrandTalkTemplateListResult result = getBrandTalkTemplateListService.getTemplateList(SENDER_KEY, 1, 10);

        // then
        assertThat(result.pageInfo().currentPage()).isEqualTo(1);
        assertThat(result.pageInfo().pageSize()).isEqualTo(10);
        assertThat(result.pageInfo().total()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().templateCode()).isEqualTo("TPL001");
        verifyNoInteractions(saveBrandTalkTemplatePort);
        verifyNoInteractions(updateBrandTalkTemplatePort);
    }

    @Test
    @DisplayName("로컬 DB에 없는 신규 템플릿은 저장한다")
    void shouldSaveNewTemplateWhenNotExistsLocally() {
        // given
        GetBrandTalkTemplateListResult.ExternalListResponse externalResponse =
                new GetBrandTalkTemplateListResult.ExternalListResponse(
                        new GetBrandTalkTemplateListResult.PageInfo(1, 10, 1),
                        List.of(new GetBrandTalkTemplateListResult.ExternalTemplateItem(
                                "TPL_NEW", "신규 템플릿", "TEXT", "O", List.of(), "2026-10-11 12:00:00"
                        ))
                );
        when(getTemplateListExternalPort.getTemplateList(SENDER_KEY, 1, 10)).thenReturn(externalResponse);
        when(findBrandTalkTemplateListPort.findAllBySenderKey(SENDER_KEY)).thenReturn(List.of());
        when(saveBrandTalkTemplatePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        getBrandTalkTemplateListService.getTemplateList(SENDER_KEY, 1, 10);

        // then
        verify(saveBrandTalkTemplatePort).save(any(AligoBrandTalkTemplate.class));
        verifyNoInteractions(updateBrandTalkTemplatePort);
    }

    @Test
    @DisplayName("기존 템플릿의 templateStatus가 변경되면 갱신한다")
    void shouldUpdateTemplateStatusWhenChanged() {
        // given
        GetBrandTalkTemplateListResult.ExternalListResponse externalResponse =
                new GetBrandTalkTemplateListResult.ExternalListResponse(
                        new GetBrandTalkTemplateListResult.PageInfo(1, 10, 1),
                        List.of(new GetBrandTalkTemplateListResult.ExternalTemplateItem(
                                "TPL001", "주문 안내", "TEXT", "S", List.of(), "2026-10-11 12:00:00"
                        ))
                );
        when(getTemplateListExternalPort.getTemplateList(SENDER_KEY, 1, 10)).thenReturn(externalResponse);

        AligoBrandTalkTemplate localTemplate = createTemplate("TPL001", BrandTalkTemplateStatus.NORMAL);
        when(findBrandTalkTemplateListPort.findAllBySenderKey(SENDER_KEY)).thenReturn(List.of(localTemplate));

        // when
        getBrandTalkTemplateListService.getTemplateList(SENDER_KEY, 1, 10);

        // then
        verify(updateBrandTalkTemplatePort).update(localTemplate);
        assertThat(localTemplate.getTemplateStatus()).isEqualTo(BrandTalkTemplateStatus.STOPPED);
        verifyNoInteractions(saveBrandTalkTemplatePort);
    }

    @Test
    @DisplayName("페이징 파라미터가 정상 전달된다")
    void shouldPassPagingParametersCorrectly() {
        // given
        GetBrandTalkTemplateListResult.ExternalListResponse externalResponse =
                new GetBrandTalkTemplateListResult.ExternalListResponse(
                        new GetBrandTalkTemplateListResult.PageInfo(3, 5, 25),
                        List.of()
                );
        when(getTemplateListExternalPort.getTemplateList(SENDER_KEY, 3, 5)).thenReturn(externalResponse);
        when(findBrandTalkTemplateListPort.findAllBySenderKey(SENDER_KEY)).thenReturn(List.of());

        // when
        GetBrandTalkTemplateListResult result = getBrandTalkTemplateListService.getTemplateList(SENDER_KEY, 3, 5);

        // then
        assertThat(result.pageInfo().currentPage()).isEqualTo(3);
        assertThat(result.pageInfo().pageSize()).isEqualTo(5);
        assertThat(result.pageInfo().total()).isEqualTo(25);
        assertThat(result.data()).isEmpty();
        verify(getTemplateListExternalPort).getTemplateList(SENDER_KEY, 3, 5);
    }

    private AligoBrandTalkTemplate createTemplate(String templateCode, BrandTalkTemplateStatus templateStatus) {
        return AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateSnapshotState.builder()
                        .id(1L)
                        .senderKey(SENDER_KEY)
                        .templateCode(templateCode)
                        .templateName("테스트 템플릿")
                        .templateType(BrandTalkTemplateType.TEXT)
                        .templateContent("테스트 내용")
                        .templateStatus(templateStatus)
                        .build()
        );
    }
}
