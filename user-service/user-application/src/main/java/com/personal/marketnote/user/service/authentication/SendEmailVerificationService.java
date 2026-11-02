package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.user.port.in.usecase.authentication.SendEmailVerificationUseCase;
import com.personal.marketnote.user.port.out.authentication.SaveEmailVerificationCodePort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@UseCase
@RequiredArgsConstructor
public class SendEmailVerificationService implements SendEmailVerificationUseCase {
    private static final String EMAIL_VERIFICATION_CONTENT_TEMPLATE = """
            <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#222">
              <p>아래 인증 코드를 입력해 이메일 인증을 완료하세요.</p>
              <p style="font-size:18px;font-weight:700;letter-spacing:2px">인증 코드: %s</p>
              <p>이 코드는 %d분 동안만 유효하며, 한 번만 사용할 수 있습니다.</p>
              <p style="color:#666">이 코드는 개인용이며, 다른 사람과 공유하지 마세요.<br>
              고객센터에서도 절대 코드 요청을 하지 않습니다.<br>감사합니다.<br></p>
              <p style="margin-top:16px;color:#666">마켓노트 고객센터 드림</p>
              <p style=\\"color:#666\\">본 메일은 발신 전용입니다.</p>
            </div>
            """;

    private final JavaMailSender mailSender;
    private final SaveEmailVerificationCodePort saveEmailVerificationCodePort;

    @Value("${mail.from:no-reply@example.com}")
    private String fromAddress;

    @Value("${mail.sender-name:shop}")
    private String senderName;

    @Value("${mail.verification.ttl-minutes:5}")
    private int ttlMinutes;

    @Override
    public void sendEmailVerification(String email) {
        String verificationCode = RandomCodeGenerator.generateEmailVerificationCode();
        saveEmailVerificationCodePort.save(email, verificationCode, ttlMinutes);
        sendVerificationEmail(email, verificationCode);
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setFrom(fromAddress, senderName);
            helper.setSubject("[마켓노트] 이메일 인증 코드");

            String htmlBody = EMAIL_VERIFICATION_CONTENT_TEMPLATE.formatted(verificationCode, ttlMinutes);

            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailSendException("Failed to send verification email", e);
        }
    }
}
