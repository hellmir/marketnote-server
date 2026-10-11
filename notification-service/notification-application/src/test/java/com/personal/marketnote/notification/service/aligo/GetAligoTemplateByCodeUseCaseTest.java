package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAligoTemplateByCodeUseCaseTest {

    @InjectMocks
    private GetAligoTemplateByCodeService getAligoTemplateByCodeService;

    @Mock
    private FindAligoTemplatePort findAligoTemplatePort;

    @Test
    @DisplayName("템플릿 코드로 조회하면 버튼을 포함한 상세 정보를 반환한다")
    void shouldReturnTemplateDetailWithButtons() {
        // given
        String templateCode = "TPL001";
        AligoTemplate template = AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("sender-key-1")
                .templateCode(templateCode)
                .templateName("주문 완료 알림")
                .templateContent("#{이름}님의 주문이 완료되었습니다.")
                .templateTitle("주문 완료")
                .templateSubtitle("배송 안내")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .templateSecure(false)
                .templateAdvert(false)
                .inspStatus("APR")
                .templateStatus(AligoTemplateStatus.A)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of(
                        AligoTemplateButton.builder()
                                .ordering(1)
                                .name("주문 상세")
                                .linkType(AligoTemplateButtonLinkType.WL)
                                .linkMo("https://m.example.com/orders")
                                .linkPc("https://example.com/orders")
                                .build()
                ))
                .build());

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when
        GetAligoTemplateDetailResult result = getAligoTemplateByCodeService.getTemplateByCode(templateCode);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.senderKey()).isEqualTo("sender-key-1");
        assertThat(result.templateCode()).isEqualTo(templateCode);
        assertThat(result.templateName()).isEqualTo("주문 완료 알림");
        assertThat(result.templateContent()).isEqualTo("#{이름}님의 주문이 완료되었습니다.");
        assertThat(result.templateTitle()).isEqualTo("주문 완료");
        assertThat(result.templateSubtitle()).isEqualTo("배송 안내");
        assertThat(result.templateType()).isEqualTo("BA");
        assertThat(result.templateEmType()).isEqualTo("NONE");
        assertThat(result.templateSecure()).isFalse();
        assertThat(result.templateAdvert()).isFalse();
        assertThat(result.inspStatus()).isEqualTo("APR");
        assertThat(result.templateStatus()).isEqualTo("A");
        assertThat(result.buttons()).hasSize(1);
        assertThat(result.buttons().get(0).name()).isEqualTo("주문 상세");
        assertThat(result.buttons().get(0).linkType()).isEqualTo("WL");
        assertThat(result.buttons().get(0).linkMo()).isEqualTo("https://m.example.com/orders");
        assertThat(result.buttons().get(0).linkPc()).isEqualTo("https://example.com/orders");
        verify(findAligoTemplatePort).findByTemplateCode(templateCode);
    }

    @Test
    @DisplayName("버튼이 없는 템플릿을 조회하면 빈 버튼 목록을 반환한다")
    void shouldReturnEmptyButtonsWhenTemplateHasNoButtons() {
        // given
        String templateCode = "TPL002";
        AligoTemplate template = AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(2L)
                .senderKey("sender-key-1")
                .templateCode(templateCode)
                .templateName("간단 알림")
                .templateContent("안녕하세요.")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus("REG")
                .templateStatus(AligoTemplateStatus.R)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of())
                .build());

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when
        GetAligoTemplateDetailResult result = getAligoTemplateByCodeService.getTemplateByCode(templateCode);

        // then
        assertThat(result.templateCode()).isEqualTo(templateCode);
        assertThat(result.buttons()).isEmpty();
        verify(findAligoTemplatePort).findByTemplateCode(templateCode);
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 코드로 조회하면 AligoTemplateNotFoundException을 던진다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        String templateCode = "NONEXISTENT";
        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getAligoTemplateByCodeService.getTemplateByCode(templateCode))
                .isInstanceOf(AligoTemplateNotFoundException.class)
                .hasMessageContaining(templateCode);
        verify(findAligoTemplatePort).findByTemplateCode(templateCode);
    }
}
