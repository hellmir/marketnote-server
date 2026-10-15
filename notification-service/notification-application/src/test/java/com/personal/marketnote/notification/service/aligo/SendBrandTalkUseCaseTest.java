package com.personal.marketnote.notification.service.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.BrandTalkFailoverCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SendBrandTalkExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateBrandTalkMessagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendBrandTalkUseCaseTest {

    @InjectMocks
    private SendBrandTalkService sendBrandTalkService;

    @Mock
    private SaveBrandTalkMessagePort saveBrandTalkMessagePort;

    @Mock
    private UpdateBrandTalkMessagePort updateBrandTalkMessagePort;

    @Mock
    private SendBrandTalkExternalPort sendBrandTalkExternalPort;

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("정상 전송 플로우: 로컬 저장(REQUESTED) → 외부 호출 → markAsSent")
    void shouldSendBrandTalkSuccessfully() {
        // given
        SendBrandTalkCommand command = createCommand("M", false);

        when(saveBrandTalkMessagePort.save(any())).thenAnswer(invocation -> {
            AligoBrandTalkMessage msg = invocation.getArgument(0);
            return AligoBrandTalkMessage.from(
                    AligoBrandTalkMessageSnapshotState.builder()
                            .id(1L)
                            .senderKey(msg.getSenderKey())
                            .sender(msg.getSender())
                            .kakaoTarget(msg.getKakaoTarget())
                            .advertYn(msg.isAdvertYn())
                            .templateCode(msg.getTemplateCode())
                            .messageStatus(BrandTalkMessageStatus.REQUESTED)
                            .successCount(0)
                            .failCount(0)
                            .failoverEnabled(msg.isFailoverEnabled())
                            .requestedAt(msg.getRequestedAt())
                            .recipients(List.of(
                                    AligoBrandTalkMessageRecipientSnapshotState.builder()
                                            .id(1L)
                                            .receiver("01012345678")
                                            .receiverMessage("{\"#{name}\":\"홍길동\"}")
                                            .recipientStatus(AligoMessageRecipientStatus.PENDING)
                                            .build()
                            ))
                            .build()
            );
        });

        when(sendBrandTalkExternalPort.send(any(), anyList(), anyList()))
                .thenReturn(new SendBrandTalkExternalPort.BrandTalkSendResponse(
                        "mid123", 1, 0, "BW", 12, 12
                ));

        // when
        SendBrandTalkResult result = sendBrandTalkService.sendBrandTalk(command);

        // then
        assertThat(result.messageId()).isEqualTo(1L);
        assertThat(result.aligoMid()).isEqualTo("mid123");
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(0);
        assertThat(result.costType()).isEqualTo("BWF");
        assertThat(result.unitCost()).isEqualTo(12);
        assertThat(result.totalCost()).isEqualTo(12);

        verify(saveBrandTalkMessagePort).save(any(AligoBrandTalkMessage.class));
        verify(sendBrandTalkExternalPort).send(any(), anyList(), anyList());
        verify(updateBrandTalkMessagePort).update(any(AligoBrandTalkMessage.class));
    }

    @Test
    @DisplayName("kakao_target 누락 시 예외")
    void shouldThrowWhenKakaoTargetMissing() {
        // given
        SendBrandTalkCommand command = new SendBrandTalkCommand(
                "sender-key", "01000000000", "TPL001", null, false,
                List.of(new BrandTalkRecipientCommand("01012345678", Map.of("#{name}", "홍길동"))),
                null
        );

        // when & then
        assertThatThrownBy(() -> sendBrandTalkService.sendBrandTalk(command))
                .isInstanceOf(InvalidBrandTalkKakaoTargetException.class);
    }

    @Test
    @DisplayName("외부 API 실패 시 markAsFailed 후 예외")
    void shouldMarkAsFailedWhenExternalApiFails() {
        // given
        SendBrandTalkCommand command = createCommand("M", false);

        when(saveBrandTalkMessagePort.save(any())).thenAnswer(invocation -> {
            AligoBrandTalkMessage msg = invocation.getArgument(0);
            return AligoBrandTalkMessage.from(
                    AligoBrandTalkMessageSnapshotState.builder()
                            .id(1L)
                            .senderKey(msg.getSenderKey())
                            .sender(msg.getSender())
                            .kakaoTarget(msg.getKakaoTarget())
                            .templateCode(msg.getTemplateCode())
                            .messageStatus(BrandTalkMessageStatus.REQUESTED)
                            .requestedAt(msg.getRequestedAt())
                            .recipients(List.of(
                                    AligoBrandTalkMessageRecipientSnapshotState.builder()
                                            .id(1L)
                                            .receiver("01012345678")
                                            .receiverMessage("{}")
                                            .recipientStatus(AligoMessageRecipientStatus.PENDING)
                                            .build()
                            ))
                            .build()
            );
        });

        when(sendBrandTalkExternalPort.send(any(), anyList(), anyList()))
                .thenThrow(new AligoApiRequestFailedException("포인트 부족"));

        // when & then
        assertThatThrownBy(() -> sendBrandTalkService.sendBrandTalk(command))
                .isInstanceOf(AligoApiRequestFailedException.class);

        verify(saveBrandTalkMessagePort).save(any());
        verify(updateBrandTalkMessagePort).update(any());
    }

    @Test
    @DisplayName("수신자 500명 초과 시 예외")
    void shouldThrowWhenRecipientsExceed500() {
        // given
        List<BrandTalkRecipientCommand> recipients = java.util.stream.IntStream.rangeClosed(1, 501)
                .mapToObj(i -> new BrandTalkRecipientCommand("010" + String.format("%08d", i), Map.of()))
                .toList();

        SendBrandTalkCommand command = new SendBrandTalkCommand(
                "sender-key", "01000000000", "TPL001", "M", false, recipients, null
        );

        // when & then
        assertThatThrownBy(() -> sendBrandTalkService.sendBrandTalk(command))
                .isInstanceOf(InvalidBrandTalkMessageRecipientCountException.class);
    }

    @Test
    @DisplayName("costType BWF/BNF 응답 필드 저장 검증")
    void shouldStoreCostTypeFromApiResponse() {
        // given
        SendBrandTalkCommand command = createCommand("N", false);

        when(saveBrandTalkMessagePort.save(any())).thenAnswer(invocation -> {
            AligoBrandTalkMessage msg = invocation.getArgument(0);
            return AligoBrandTalkMessage.from(
                    AligoBrandTalkMessageSnapshotState.builder()
                            .id(2L)
                            .senderKey(msg.getSenderKey())
                            .sender(msg.getSender())
                            .kakaoTarget(msg.getKakaoTarget())
                            .templateCode(msg.getTemplateCode())
                            .messageStatus(BrandTalkMessageStatus.REQUESTED)
                            .requestedAt(msg.getRequestedAt())
                            .recipients(List.of(
                                    AligoBrandTalkMessageRecipientSnapshotState.builder()
                                            .id(1L)
                                            .receiver("01012345678")
                                            .receiverMessage("{}")
                                            .recipientStatus(AligoMessageRecipientStatus.PENDING)
                                            .build()
                            ))
                            .build()
            );
        });

        when(sendBrandTalkExternalPort.send(any(), anyList(), anyList()))
                .thenReturn(new SendBrandTalkExternalPort.BrandTalkSendResponse(
                        "mid456", 1, 0, "BN", 8, 8
                ));

        // when
        SendBrandTalkResult result = sendBrandTalkService.sendBrandTalk(command);

        // then
        assertThat(result.costType()).isEqualTo("BNF");
        assertThat(result.unitCost()).isEqualTo(8);
    }

    private SendBrandTalkCommand createCommand(String kakaoTarget, boolean failoverEnabled) {
        BrandTalkFailoverCommand failover = failoverEnabled
                ? new BrandTalkFailoverCommand(true, "대체 제목", "SMS", List.of(), List.of())
                : null;

        return new SendBrandTalkCommand(
                "sender-key", "01000000000", "TPL001", kakaoTarget, false,
                List.of(new BrandTalkRecipientCommand("01012345678", Map.of("#{name}", "홍길동"))),
                failover
        );
    }
}
