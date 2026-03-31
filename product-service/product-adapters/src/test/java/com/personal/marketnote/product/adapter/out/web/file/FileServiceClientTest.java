package com.personal.marketnote.product.adapter.out.web.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.exception.FileServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    @DisplayName("findImagesByProductIdAndSort")
    class FindImagesByProductIdAndSort {

        @Test
        @DisplayName("상품 이미지 조회에 성공하면 GetFilesResult를 반환한다")
        void shouldReturnFilesResultWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=PRODUCT&ownerId=1&sort=PRODUCT_CATALOG_IMAGE"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "files": [
                                        {
                                            "id": 1,
                                            "sort": "PRODUCT_THUMBNAIL",
                                            "extension": "jpg",
                                            "name": "image1.jpg",
                                            "storageUrl": "https://s3.example.com/image1.jpg",
                                            "resizedStorageUrls": ["https://s3.example.com/image1_200.jpg"],
                                            "orderNum": 1
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByProductIdAndSort(1L, FileSort.PRODUCT_CATALOG_IMAGE);

            assertThat(result).isPresent();
            assertThat(result.get().images()).hasSize(1);
            assertThat(result.get().images().get(0).name()).isEqualTo("image1.jpg");

            mockServer.verify();
        }

        @Test
        @DisplayName("응답에 파일이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenNoFiles() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=PRODUCT&ownerId=1&sort=PRODUCT_CATALOG_IMAGE"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"content": {"files": null}}
                            """, MediaType.APPLICATION_JSON));

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByProductIdAndSort(1L, FileSort.PRODUCT_CATALOG_IMAGE);

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/files?ownerType=PRODUCT&ownerId=1&sort=PRODUCT_CATALOG_IMAGE"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Optional<GetFilesResult> result =
                    fileServiceClient.findImagesByProductIdAndSort(1L, FileSort.PRODUCT_CATALOG_IMAGE);

            assertThat(result).isEmpty();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("파일 삭제 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenDeleteSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files/1"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withSuccess());

            fileServiceClient.delete(1L);

            mockServer.verify();
        }

        @Test
        @DisplayName("파일 삭제 요청이 실패하면 FileServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenDeleteFails() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/files/1"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withServerError());

            assertThatThrownBy(() -> fileServiceClient.delete(1L))
                    .isInstanceOf(FileServiceRequestFailedException.class);

            mockServer.verify();
        }
    }
}
