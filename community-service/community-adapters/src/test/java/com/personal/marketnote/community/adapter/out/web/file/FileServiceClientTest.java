package com.personal.marketnote.community.adapter.out.web.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class FileServiceClientTest {

    private static final String BASE_URL = "http://localhost:9000";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private FileServiceClient fileServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @Mock
    private ServiceCommunicationRecorder serviceCommunicationRecorder;

    @Mock
    private ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        fileServiceClient = new FileServiceClient(
                builder,
                BASE_URL,
                hmacServiceAuthHeaderBuilder,
                serviceCommunicationRecorder,
                serviceCommunicationPayloadGenerator
        );

        lenient().when(serviceCommunicationPayloadGenerator.buildRequestPayloadJson(any(), any(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
        lenient().when(serviceCommunicationPayloadGenerator.buildErrorPayloadJson(anyString(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
    }

    @Nested
    @DisplayName("findImagesByReviewIdAndSort")
    class FindImagesByReviewIdAndSort {

        @Test
        @DisplayName("리뷰 이미지 조회에 성공하면 GetFilesResult를 반환한다")
        void shouldReturnFilesResultWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=REVIEW&ownerId=1&sort=REVIEW_IMAGE"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "files": [
                                        {
                                            "id": 1,
                                            "sort": "REVIEW_IMAGE",
                                            "extension": "jpg",
                                            "name": "review1.jpg",
                                            "s3Url": "https://s3.example.com/review1.jpg",
                                            "resizedS3Urls": ["https://s3.example.com/review1_200.jpg"],
                                            "orderNum": 1
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByReviewIdAndSort(1L, FileSort.REVIEW_IMAGE);

            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().get(0).name()).isEqualTo("review1.jpg");

            mockServer.verify();
        }

        @Test
        @DisplayName("응답에 파일이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenNoFiles() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=REVIEW&ownerId=1&sort=REVIEW_IMAGE"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"content": {"files": null}}
                            """, MediaType.APPLICATION_JSON));

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByReviewIdAndSort(1L, FileSort.REVIEW_IMAGE);

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=REVIEW&ownerId=1&sort=REVIEW_IMAGE"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByReviewIdAndSort(1L, FileSort.REVIEW_IMAGE);

            assertThat(result).isEmpty();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("findImagesByPostIdAndSort")
    class FindImagesByPostIdAndSort {

        @Test
        @DisplayName("게시글 이미지 조회에 성공하면 GetFilesResult를 반환한다")
        void shouldReturnFilesResultWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=POST&ownerId=2&sort=POST_IMAGE"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "files": [
                                        {
                                            "id": 2,
                                            "sort": "POST_IMAGE",
                                            "extension": "png",
                                            "name": "post1.png",
                                            "s3Url": "https://s3.example.com/post1.png",
                                            "resizedS3Urls": [],
                                            "orderNum": 1
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByPostIdAndSort(2L, FileSort.POST_IMAGE);

            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().get(0).name()).isEqualTo("post1.png");

            mockServer.verify();
        }
    }
}
