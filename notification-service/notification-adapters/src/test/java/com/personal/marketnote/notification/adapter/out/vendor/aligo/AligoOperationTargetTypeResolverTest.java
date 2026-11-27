package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AligoOperationTargetTypeResolverTest {

    private AligoOperationTargetTypeResolver resolver;
    private Logger resolverLogger;
    private ListAppender<ILoggingEvent> appender;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        resolver = new AligoOperationTargetTypeResolver();
        resolverLogger = (Logger) LoggerFactory.getLogger(AligoOperationTargetTypeResolver.class);
        previousLevel = resolverLogger.getLevel();
        resolverLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        resolverLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        resolverLogger.detachAppender(appender);
        appender.stop();
        resolverLogger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("sendAlimTalk operation은 ALIMTALK targetType으로 매핑된다")
    void shouldResolveAlimTalk() {
        assertThat(resolver.resolve("sendAlimTalk"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK);
    }

    @Test
    @DisplayName("sendBrandTalk operation은 FRIEND_TALK targetType으로 매핑된다")
    void shouldResolveFriendTalk() {
        assertThat(resolver.resolve("sendBrandTalk"))
            .isEqualTo(NotificationVendorCommunicationTargetType.FRIEND_TALK);
    }

    @Test
    @DisplayName("createBrandTalkTemplate/getBrandTalkTemplateList operation은 FRIEND_TALK_TEMPLATE targetType으로 매핑된다")
    void shouldResolveFriendTalkTemplate() {
        assertThat(resolver.resolve("createBrandTalkTemplate"))
            .isEqualTo(NotificationVendorCommunicationTargetType.FRIEND_TALK_TEMPLATE);
        assertThat(resolver.resolve("getBrandTalkTemplateList"))
            .isEqualTo(NotificationVendorCommunicationTargetType.FRIEND_TALK_TEMPLATE);
    }

    @Test
    @DisplayName("requestSenderAuthentication/registerSenderProfile/getSenderProfileList operation은 SENDER_PROFILE로 매핑된다")
    void shouldResolveSenderProfile() {
        assertThat(resolver.resolve("requestSenderAuthentication"))
            .isEqualTo(NotificationVendorCommunicationTargetType.SENDER_PROFILE);
        assertThat(resolver.resolve("registerSenderProfile"))
            .isEqualTo(NotificationVendorCommunicationTargetType.SENDER_PROFILE);
        assertThat(resolver.resolve("getSenderProfileList"))
            .isEqualTo(NotificationVendorCommunicationTargetType.SENDER_PROFILE);
    }

    @Test
    @DisplayName("getHeartInfo operation은 BALANCE targetType으로 매핑된다")
    void shouldResolveBalance() {
        assertThat(resolver.resolve("getHeartInfo"))
            .isEqualTo(NotificationVendorCommunicationTargetType.BALANCE);
    }

    @Test
    @DisplayName("cancelMessage operation은 CANCEL targetType으로 매핑된다")
    void shouldResolveCancel() {
        assertThat(resolver.resolve("cancelMessage"))
            .isEqualTo(NotificationVendorCommunicationTargetType.CANCEL);
    }

    @Test
    @DisplayName("getMessageHistory/getMessageDetail operation은 SEND_HISTORY targetType으로 매핑된다")
    void shouldResolveSendHistory() {
        assertThat(resolver.resolve("getMessageHistory"))
            .isEqualTo(NotificationVendorCommunicationTargetType.SEND_HISTORY);
        assertThat(resolver.resolve("getMessageDetail"))
            .isEqualTo(NotificationVendorCommunicationTargetType.SEND_HISTORY);
    }

    @Test
    @DisplayName("템플릿 관련 5개 operation은 ALIMTALK_TEMPLATE targetType으로 매핑된다")
    void shouldResolveAlimTalkTemplate() {
        assertThat(resolver.resolve("getCategoryTree"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
        assertThat(resolver.resolve("registerTemplate"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
        assertThat(resolver.resolve("updateTemplate"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
        assertThat(resolver.resolve("getTemplateList"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
        assertThat(resolver.resolve("requestTemplateInspection"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
        assertThat(resolver.resolve("deleteTemplate"))
            .isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE);
    }

    @Test
    @DisplayName("등록되지 않은 operation은 fail-open으로 ALIMTALK 기본값을 반환하고 ERROR 로그를 남긴다")
    void shouldReturnDefaultAndLogErrorOnUnknownOperation() {
        NotificationVendorCommunicationTargetType result = resolver.resolve("unknownOperation");

        assertThat(result).isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK);
        boolean hasErrorLog = appender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("unknownOperation"));
        assertThat(hasErrorLog).isTrue();
    }

    @Test
    @DisplayName("null operation은 fail-open으로 ALIMTALK 기본값을 반환한다")
    void shouldReturnDefaultOnNullOperation() {
        NotificationVendorCommunicationTargetType result = resolver.resolve(null);

        assertThat(result).isEqualTo(NotificationVendorCommunicationTargetType.ALIMTALK);
    }
}
