package com.personal.marketnote.common.adapter.in.web.identification;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.web.identification.apidocs.WhoAmIApiDocs;
import com.personal.marketnote.common.adapter.in.web.identification.response.WhoAmIResponse;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 서버 정보 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 서버 정보 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/identification")
@Tag(name = "서버 정보 API", description = "서버 정보 관련 API")
@RequiredArgsConstructor
@Slf4j
public class IdentificationController {
    /**
     * (관리자) 자신의 공인 IP 조회
     *
     * @return 공인 IP 조회 응답 {@link String}
     * @Author 성효빈
     * @Date 2026-01-21
     * @Description 자신의 공인 IP를 조회합니다.
     */
    @GetMapping("/who-am-i")
    @PreAuthorize(ADMIN_POINTCUT)
    @WhoAmIApiDocs
    public ResponseEntity<BaseResponse<WhoAmIResponse>> whoAmI() {
        return new ResponseEntity<>(
                BaseResponse.of(
                        WhoAmIResponse.of(fetchPublicIp()),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "자신의 공인 IP 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    private String fetchPublicIp() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://checkip.amazonaws.com"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return "UNKNOWN";
            }

            String body = response.body();
            if (!FormatValidator.hasValue(body)) {
                return "UNKNOWN";
            }

            return body.trim();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
