package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterBrandTalkTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterBrandTalkTemplateResponse;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetBrandTalkTemplateListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterBrandTalkTemplateUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AligoBrandTalkTemplateControllerTest {

    @InjectMocks
    private AligoBrandTalkTemplateController controller;

    @Mock
    private RegisterBrandTalkTemplateUseCase registerBrandTalkTemplateUseCase;

    @Mock
    private GetBrandTalkTemplateListUseCase getBrandTalkTemplateListUseCase;

    private RegisterBrandTalkTemplateRequest buildRequest() {
        return new RegisterBrandTalkTemplateRequest(
                "sk-1", "CODE-1", "템플릿명", "BA", "본문", false,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    private MultipartFile buildImage(String originalFilename) {
        return new MockMultipartFile(
                "image", originalFilename, MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3}
        );
    }

    private MultipartFile buildCarouselItemImage(String originalFilename) {
        return new MockMultipartFile(
                "carouselItemImages", originalFilename, MediaType.IMAGE_JPEG_VALUE, new byte[]{4, 5}
        );
    }

    private void stubRegister() {
        when(registerBrandTalkTemplateUseCase.registerTemplate(any()))
                .thenReturn(new RegisterBrandTalkTemplateResult(1L, "CODE-1", "템플릿명", "BA"));
    }

    private RegisterBrandTalkTemplateCommand captureCommand() {
        ArgumentCaptor<RegisterBrandTalkTemplateCommand> captor =
                ArgumentCaptor.forClass(RegisterBrandTalkTemplateCommand.class);
        verify(registerBrandTalkTemplateUseCase).registerTemplate(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("registerTemplate - image 파일명 sanitize")
    class RegisterTemplateImageSanitize {

        @Test
        @DisplayName("image 파일명에 경로 구분자가 포함되면 제거한 sanitized 파일명으로 전달한다")
        void shouldStripPathSeparatorsFromImageFileName() throws IOException {
            // given
            stubRegister();
            MultipartFile image = buildImage("../../../etc/passwd.jpg");

            // when
            ResponseEntity<BaseResponse<RegisterBrandTalkTemplateResponse>> response =
                    controller.registerTemplate(buildRequest(), image, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.imageFileName()).isEqualTo("......etcpasswd.jpg");
        }

        @Test
        @DisplayName("image 파일명의 HTML 태그/특수문자는 제거된 상태로 Command에 전달된다")
        void shouldStripHtmlTagsFromImageFileName() throws IOException {
            // given
            stubRegister();
            MultipartFile image = buildImage("<script>alert(1)</script>.jpg");

            // when
            controller.registerTemplate(buildRequest(), image, null);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.imageFileName()).isEqualTo("scriptalert1script.jpg");
        }

        @Test
        @DisplayName("image 파일명의 공백은 하이픈(-)으로 치환되어 Command에 전달된다")
        void shouldReplaceWhitespaceWithHyphenInImageFileName() throws IOException {
            // given
            stubRegister();
            MultipartFile image = buildImage("my image file.png");

            // when
            controller.registerTemplate(buildRequest(), image, null);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.imageFileName()).isEqualTo("my-image-file.png");
        }

        @Test
        @DisplayName("image 파일명의 한글 등 허용되지 않는 유니코드 문자는 제거된 상태로 Command에 전달된다")
        void shouldStripNonAsciiCharactersFromImageFileName() throws IOException {
            // given
            stubRegister();
            MultipartFile image = buildImage("이미지파일.png");

            // when
            controller.registerTemplate(buildRequest(), image, null);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.imageFileName()).isEqualTo(".png");
        }

        @Test
        @DisplayName("image가 null이면 Command의 imageFileName은 null이다")
        void shouldSetNullImageFileNameWhenImageMissing() throws IOException {
            // given
            stubRegister();

            // when
            controller.registerTemplate(buildRequest(), null, null);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.imageFileName()).isNull();
            assertThat(command.imageFile()).isNull();
        }

    }

    @Nested
    @DisplayName("registerTemplate - carouselItemImages 파일명 sanitize")
    class RegisterTemplateCarouselItemImagesSanitize {

        @Test
        @DisplayName("carouselItemImages 각 파일명에 sanitize가 적용된 파일명 목록으로 전달된다")
        void shouldSanitizeEachCarouselItemImageFileName() throws IOException {
            // given
            stubRegister();
            List<MultipartFile> carouselItemImages = List.of(
                    buildCarouselItemImage("../evil.jpg"),
                    buildCarouselItemImage("safe image.png"),
                    buildCarouselItemImage("<b>bold</b>.gif")
            );

            // when
            controller.registerTemplate(buildRequest(), null, carouselItemImages);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.carouselItemImageFileNames()).containsExactly(
                    "..evil.jpg",
                    "safe-image.png",
                    "bboldb.gif"
            );
        }

        @Test
        @DisplayName("carouselItemImages가 null이면 Command의 파일명 목록은 빈 리스트다")
        void shouldReturnEmptyCarouselFileNamesWhenImagesMissing() throws IOException {
            // given
            stubRegister();

            // when
            controller.registerTemplate(buildRequest(), null, null);

            // then
            RegisterBrandTalkTemplateCommand command = captureCommand();
            assertThat(command.carouselItemImageFileNames()).isEmpty();
            assertThat(command.carouselItemImageFiles()).isEmpty();
        }
    }
}
