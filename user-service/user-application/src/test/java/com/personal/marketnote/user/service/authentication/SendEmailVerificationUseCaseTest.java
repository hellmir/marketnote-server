package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.user.port.out.authentication.SaveEmailVerificationCodePort;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendEmailVerificationUseCaseTest {
    private static final String FROM_ADDRESS = "no-reply@test.com";
    private static final String SENDER_NAME = "marketnote";
    private static final int TTL_MINUTES = 5;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SaveEmailVerificationCodePort saveEmailVerificationCodePort;

    @InjectMocks
    private SendEmailVerificationService sendEmailVerificationService;

    @BeforeEach
    void setUp() throws Exception {
        setPrivateField("fromAddress", FROM_ADDRESS);
        setPrivateField("senderName", SENDER_NAME);
        setPrivateField("ttlMinutes", TTL_MINUTES);
    }

    @Test
    @DisplayName("이메일 인증 요청이 성공하면 인증 코드를 저장하고 이메일을 전송한다")
    void sendEmailVerification_success() throws Exception {
        // given
        String email = "user@test.com";
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(message);

        // when
        sendEmailVerificationService.sendEmailVerification(email);

        // then
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        InOrder inOrder = inOrder(saveEmailVerificationCodePort, mailSender);
        inOrder.verify(saveEmailVerificationCodePort).save(eq(email), codeCaptor.capture(), eq(TTL_MINUTES));
        inOrder.verify(mailSender).send(message);

        String savedCode = codeCaptor.getValue();
        assertThat(savedCode).matches("\\d{6}");

        Object content = message.getContent();
        assertThat(content).isInstanceOf(String.class);
        assertThat((String) content).contains(savedCode);
        assertThat((String) content).contains(String.valueOf(TTL_MINUTES));

        verify(mailSender).createMimeMessage();
        verifyNoMoreInteractions(saveEmailVerificationCodePort, mailSender);
    }

    @Test
    @DisplayName("인증 코드 저장에 실패하면 이메일 전송을 시도하지 않는다")
    void sendEmailVerification_saveFails_throws() {
        // given
        String email = "user@test.com";
        RuntimeException exception = new RuntimeException("save failed");

        doThrow(exception).when(saveEmailVerificationCodePort).save(eq(email), anyString(), eq(TTL_MINUTES));

        // expect
        assertThatThrownBy(() -> sendEmailVerificationService.sendEmailVerification(email))
                .isSameAs(exception);

        verify(saveEmailVerificationCodePort).save(eq(email), anyString(), eq(TTL_MINUTES));
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("이메일 전송에 실패하면 MailSendException을 던진다")
    void sendEmailVerification_mailSendFails_throws() {
        // given
        String email = "user@test.com";
        MimeMessage message = new FailingMimeMessage(Session.getInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(message);

        // expect
        assertThatThrownBy(() -> sendEmailVerificationService.sendEmailVerification(email))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("Failed to send verification email");

        verify(saveEmailVerificationCodePort).save(eq(email), anyString(), eq(TTL_MINUTES));
        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
        verifyNoMoreInteractions(saveEmailVerificationCodePort, mailSender);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = SendEmailVerificationService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sendEmailVerificationService, value);
    }

    private static class FailingMimeMessage extends MimeMessage {
        private FailingMimeMessage(Session session) {
            super(session);
        }

        @Override
        public void setFrom(Address address) throws MessagingException {
            throw new MessagingException("failed");
        }
    }
}
