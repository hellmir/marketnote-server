package com.personal.marketnote.common.configuration.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaErrorHandler 테스트")
class KafkaErrorHandlerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaErrorHandler kafkaErrorHandler;

    @BeforeEach
    void setUp() {
        kafkaErrorHandler = new KafkaErrorHandler();
    }

    @Test
    @DisplayName("commonErrorHandler는 DefaultErrorHandler 인스턴스를 반환한다")
    void commonErrorHandler_returnsDefaultErrorHandler() {
        // given
        DeadLetterPublishingRecoverer recoverer = kafkaErrorHandler.deadLetterPublishingRecoverer(kafkaTemplate);

        // when
        CommonErrorHandler errorHandler = kafkaErrorHandler.commonErrorHandler(recoverer);

        // then
        assertThat(errorHandler).isInstanceOf(DefaultErrorHandler.class);
    }

    @Test
    @DisplayName("dltErrorHandler는 DefaultErrorHandler 인스턴스를 반환한다")
    void dltErrorHandler_returnsDefaultErrorHandler() {
        // given & when
        CommonErrorHandler dltErrorHandler = kafkaErrorHandler.dltErrorHandler();

        // then
        assertThat(dltErrorHandler).isInstanceOf(DefaultErrorHandler.class);
    }

    @Test
    @DisplayName("dltErrorHandler는 KafkaTemplate 의존 없이 생성된다")
    void dltErrorHandler_createdWithoutKafkaTemplateDependency() {
        // given & when — KafkaTemplate 없이 dltErrorHandler 생성 가능
        CommonErrorHandler dltErrorHandler = kafkaErrorHandler.dltErrorHandler();

        // then — commonErrorHandler와 달리 KafkaTemplate/DeadLetterPublishingRecoverer 불필요
        assertThat(dltErrorHandler).isNotNull();
    }
}
