package com.personal.marketnote.notification.service.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort.AligoAlimTalkSendResponse;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendAligoAlimTalkUseCaseTest {

    @InjectMocks
    private SendAligoAlimTalkService sendAligoAlimTalkService;

    @Mock
    private FindAligoTemplatePort findAligoTemplatePort;

    @Mock
    private SaveAligoMessagePort saveAligoMessagePort;

    @Mock
    private UpdateAligoMessagePort updateAligoMessagePort;

    @Mock
    private SendAligoAlimTalkExternalPort sendAligoAlimTalkExternalPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("정상 발송 플로우: 로컬 저장(REQUESTED) 후 외부 호출 성공 시 markAsSent로 상태 전이한다")
    void shouldSendAlimTalkSuccessfully() {
        // given
        String templateCode = "TPL_WELCOME";
        AligoTemplate template = createApprovedTemplate(templateCode, "안녕하세요 #{이름}님, #{상품명}을 구매해주셔서 감사합니다.");

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of("이름", "홍길동", "상품명", "운동화"),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "홍길동"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));
        when(saveAligoMessagePort.save(any(AligoMessage.class))).thenAnswer(invocation -> {
            AligoMessage msg = invocation.getArgument(0);
            return AligoMessage.from(AligoMessageSnapshotState.builder()
                    .id(100L)
                    .senderKey(msg.getSenderKey())
                    .templateCode(msg.getTemplateCode())
                    .sender(msg.getSender())
                    .messageType(msg.getMessageType())
                    .content(msg.getContent())
                    .title(msg.getTitle())
                    .buttons(msg.getButtons())
                    .messageStatus(AligoMessageStatus.REQUESTED)
                    .successCount(0)
                    .failCount(0)
                    .requestedAt(msg.getRequestedAt())
                    .status(EntityStatus.ACTIVE)
                    .build());
        });
        when(sendAligoAlimTalkExternalPort.send(any(AligoMessage.class)))
                .thenReturn(new AligoAlimTalkSendResponse("MID_001", 1, 0));

        // when
        SendAligoAlimTalkResult result = sendAligoAlimTalkService.sendAlimTalk(command);

        // then
        assertThat(result.messageId()).isEqualTo(100L);
        assertThat(result.aligoMid()).isEqualTo("MID_001");
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isZero();

        verify(saveAligoMessagePort).save(any(AligoMessage.class));
        verify(sendAligoAlimTalkExternalPort).send(any(AligoMessage.class));
        verify(updateAligoMessagePort).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("템플릿이 APR(승인) 상태가 아닐 때 AligoTemplateMismatchException을 던진다")
    void shouldThrowExceptionWhenTemplateIsNotApproved() {
        // given
        String templateCode = "TPL_PENDING";
        AligoTemplate template = createTemplateWithInspStatus(templateCode, "REG");

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "테스트"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() -> sendAligoAlimTalkService.sendAlimTalk(command))
                .isInstanceOf(AligoTemplateMismatchException.class);

        verifyNoInteractions(saveAligoMessagePort);
        verifyNoInteractions(sendAligoAlimTalkExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("템플릿 본문에 미해결 변수가 있을 때 InvalidAligoTemplateException을 던진다")
    void shouldThrowExceptionWhenVariableNotResolved() {
        // given
        String templateCode = "TPL_VAR";
        AligoTemplate template = createApprovedTemplate(templateCode, "안녕하세요 #{이름}님, #{주문번호} 확인해주세요.");

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of("이름", "홍길동"),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "홍길동"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() -> sendAligoAlimTalkService.sendAlimTalk(command))
                .isInstanceOf(InvalidAligoTemplateException.class);

        verifyNoInteractions(saveAligoMessagePort);
        verifyNoInteractions(sendAligoAlimTalkExternalPort);
    }

    @Test
    @DisplayName("포인트 부족(-99) 응답 시 AligoInsufficientPointException을 전파하고 markAsFailed를 수행한다")
    void shouldPropagateInsufficientPointExceptionAndMarkAsFailed() {
        // given
        String templateCode = "TPL_POINT";
        AligoTemplate template = createApprovedTemplate(templateCode, "알림 내용입니다.");

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "테스트"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));
        when(saveAligoMessagePort.save(any(AligoMessage.class))).thenAnswer(invocation -> {
            AligoMessage msg = invocation.getArgument(0);
            return AligoMessage.from(AligoMessageSnapshotState.builder()
                    .id(200L)
                    .senderKey(msg.getSenderKey())
                    .templateCode(msg.getTemplateCode())
                    .sender(msg.getSender())
                    .messageType(msg.getMessageType())
                    .content(msg.getContent())
                    .title(msg.getTitle())
                    .messageStatus(AligoMessageStatus.REQUESTED)
                    .successCount(0)
                    .failCount(0)
                    .requestedAt(msg.getRequestedAt())
                    .status(EntityStatus.ACTIVE)
                    .build());
        });
        when(sendAligoAlimTalkExternalPort.send(any(AligoMessage.class)))
                .thenThrow(new AligoInsufficientPointException());

        // when & then
        assertThatThrownBy(() -> sendAligoAlimTalkService.sendAlimTalk(command))
                .isInstanceOf(AligoInsufficientPointException.class);

        verify(updateAligoMessagePort).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("500명 수신자 발송에 성공한다")
    void shouldSendAlimTalkWith500Recipients() {
        // given
        String templateCode = "TPL_MASS";
        AligoTemplate template = createApprovedTemplate(templateCode, "대량 발송 내용입니다.");

        List<AligoAlimTalkRecipientCommand> recipients = IntStream.rangeClosed(1, 500)
                .mapToObj(i -> new AligoAlimTalkRecipientCommand("010" + String.format("%08d", i), "수신자" + i))
                .toList();

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                recipients
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));
        when(saveAligoMessagePort.save(any(AligoMessage.class))).thenAnswer(invocation -> {
            AligoMessage msg = invocation.getArgument(0);
            return AligoMessage.from(AligoMessageSnapshotState.builder()
                    .id(300L)
                    .senderKey(msg.getSenderKey())
                    .templateCode(msg.getTemplateCode())
                    .sender(msg.getSender())
                    .messageType(msg.getMessageType())
                    .content(msg.getContent())
                    .title(msg.getTitle())
                    .messageStatus(AligoMessageStatus.REQUESTED)
                    .successCount(0)
                    .failCount(0)
                    .requestedAt(msg.getRequestedAt())
                    .status(EntityStatus.ACTIVE)
                    .build());
        });
        when(sendAligoAlimTalkExternalPort.send(any(AligoMessage.class)))
                .thenReturn(new AligoAlimTalkSendResponse("MID_MASS", 500, 0));

        // when
        SendAligoAlimTalkResult result = sendAligoAlimTalkService.sendAlimTalk(command);

        // then
        assertThat(result.successCount()).isEqualTo(500);
        assertThat(result.failCount()).isZero();
        verify(saveAligoMessagePort).save(any(AligoMessage.class));
        verify(sendAligoAlimTalkExternalPort).send(any(AligoMessage.class));
        verify(updateAligoMessagePort).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("501명 수신자 발송 시 InvalidAligoMessageRecipientCountException을 던진다")
    void shouldThrowExceptionWhen501Recipients() {
        // given
        String templateCode = "TPL_OVER";
        AligoTemplate template = createApprovedTemplate(templateCode, "초과 발송 내용입니다.");

        List<AligoAlimTalkRecipientCommand> recipients = IntStream.rangeClosed(1, 501)
                .mapToObj(i -> new AligoAlimTalkRecipientCommand("010" + String.format("%08d", i), "수신자" + i))
                .toList();

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                recipients
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() -> sendAligoAlimTalkService.sendAlimTalk(command))
                .isInstanceOf(InvalidAligoMessageRecipientCountException.class);

        verifyNoInteractions(sendAligoAlimTalkExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 코드로 발송 시 AligoTemplateNotFoundException을 던진다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        String templateCode = "NONEXISTENT";
        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "테스트"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sendAligoAlimTalkService.sendAlimTalk(command))
                .isInstanceOf(AligoTemplateNotFoundException.class);

        verifyNoInteractions(saveAligoMessagePort);
        verifyNoInteractions(sendAligoAlimTalkExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("버튼이 있는 템플릿 발송 시 버튼 JSON이 직렬화되어 메시지에 포함된다")
    void shouldSerializeButtonsWhenTemplateHasButtons() {
        // given
        String templateCode = "TPL_BTN";
        AligoTemplateButton button = AligoTemplateButton.builder()
                .ordering(1)
                .name("주문 상세 보기")
                .linkType(AligoTemplateButtonLinkType.WL)
                .linkMo("https://m.example.com/order")
                .linkPc("https://www.example.com/order")
                .build();
        AligoTemplate template = createApprovedTemplateWithButtons(templateCode, "주문 완료 알림입니다.", List.of(button));

        SendAligoAlimTalkCommand command = new SendAligoAlimTalkCommand(
                templateCode,
                "01012345678",
                Map.of(),
                List.of(new AligoAlimTalkRecipientCommand("01098765432", "홍길동"))
        );

        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));
        when(saveAligoMessagePort.save(any(AligoMessage.class))).thenAnswer(invocation -> {
            AligoMessage msg = invocation.getArgument(0);
            assertThat(msg.getButtons()).contains("주문 상세 보기");
            return AligoMessage.from(AligoMessageSnapshotState.builder()
                    .id(400L)
                    .senderKey(msg.getSenderKey())
                    .templateCode(msg.getTemplateCode())
                    .sender(msg.getSender())
                    .messageType(msg.getMessageType())
                    .content(msg.getContent())
                    .title(msg.getTitle())
                    .buttons(msg.getButtons())
                    .messageStatus(AligoMessageStatus.REQUESTED)
                    .successCount(0)
                    .failCount(0)
                    .requestedAt(msg.getRequestedAt())
                    .status(EntityStatus.ACTIVE)
                    .build());
        });
        when(sendAligoAlimTalkExternalPort.send(any(AligoMessage.class)))
                .thenReturn(new AligoAlimTalkSendResponse("MID_BTN", 1, 0));

        // when
        SendAligoAlimTalkResult result = sendAligoAlimTalkService.sendAlimTalk(command);

        // then
        assertThat(result.aligoMid()).isEqualTo("MID_BTN");
        verify(saveAligoMessagePort).save(any(AligoMessage.class));
    }

    private AligoTemplate createApprovedTemplate(String templateCode, String content) {
        return AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("test-sender-key")
                .templateCode(templateCode)
                .templateName("테스트 템플릿")
                .templateContent(content)
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus("APR")
                .templateStatus(AligoTemplateStatus.A)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of())
                .build());
    }

    private AligoTemplate createApprovedTemplateWithButtons(
            String templateCode, String content, List<AligoTemplateButton> buttons
    ) {
        return AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("test-sender-key")
                .templateCode(templateCode)
                .templateName("테스트 템플릿")
                .templateContent(content)
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus("APR")
                .templateStatus(AligoTemplateStatus.A)
                .status(EntityStatus.ACTIVE)
                .buttons(buttons)
                .build());
    }

    private AligoTemplate createTemplateWithInspStatus(String templateCode, String inspStatus) {
        return AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("test-sender-key")
                .templateCode(templateCode)
                .templateName("테스트 템플릿")
                .templateContent("안녕하세요")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus(inspStatus)
                .templateStatus(AligoTemplateStatus.R)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of())
                .build());
    }
}
