package com.personal.marketnote.user.adapter.in.web.authentication.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.adapter.exception.HashAlgorithmNotAvailableException;
import com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs.Oauth2LoginApiDocs;
import com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs.RefreshAccessTokenApiDocs;
import com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs.SendEmailVerificationApiDocs;
import com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs.ValidateVerificationCodeApiDocs;
import com.personal.marketnote.user.adapter.in.web.authentication.request.OAuth2LoginRequest;
import com.personal.marketnote.user.adapter.in.web.authentication.response.OAuth2LoginResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.RefreshedAccessTokenResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.verifyCodeResponse;
import com.personal.marketnote.user.adapter.in.web.user.mapper.UserRequestToCommandMapper;
import com.personal.marketnote.user.adapter.in.web.user.request.VerifyCodeRequest;
import com.personal.marketnote.user.adapter.out.vendor.authentication.WebBasedAuthenticationServiceAdapter;
import com.personal.marketnote.user.port.in.result.VerifyCodeResult;
import com.personal.marketnote.user.port.in.usecase.authentication.SendEmailVerificationUseCase;
import com.personal.marketnote.user.port.in.usecase.authentication.VerifyCodeUseCase;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionMessage.INVALID_EMAIL_EXCEPTION_MESSAGE;
import static com.personal.marketnote.common.utility.RegularExpressionConstant.EMAIL_PATTERN;

/**
 * 인증 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 인증 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/authentication")
@Tag(
        name = "인증 API",
        description = "인증 관련 API"
)
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final WebBasedAuthenticationServiceAdapter authServiceAdapter;
    private final SendEmailVerificationUseCase sendEmailVerificationUseCase;
    private final VerifyCodeUseCase verifyCodeUseCase;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * OAuth2 로그인
     *
     * @param authVendor OAuth2 인증 제공자
     * @param code       OAuth2 인증 코드
     * @param state      OAuth2 인증 상태
     * @param request    HTTP 요청
     * @return 인증 토큰 응답 {@link GrantedTokenInfo}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description OAuth2 로그인을 수행합니다.
     */
    @Oauth2LoginApiDocs
    @GetMapping("/{authVendor}/redirect-uri")
    public ResponseEntity<BaseResponse<GrantedTokenInfo>> oauth2Login(
            @PathVariable("authVendor") String authVendor,
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request
    ) throws UnsupportedCodeException {
        OAuth2LoginResponse response = authServiceAdapter.loginByOAuth2(
                new OAuth2LoginRequest(authVendor, code, state, request)
        );

        return new ResponseEntity<>(response.headers(), HttpStatus.PERMANENT_REDIRECT);
    }

    /**
     * Access Token 재발급
     *
     * @param refreshToken(cookie) Refresh Token; HTTP-only
     * @return 재발급된 Access Token 응답 {@link BaseResponse<RefreshedAccessTokenResponse>}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description Access Token을 재발급합니다.
     */
    @RefreshAccessTokenApiDocs
    @PostMapping("/access-token/refresh")
    public ResponseEntity<BaseResponse<RefreshedAccessTokenResponse>> refreshAccessToken(
            @CookieValue(value = "refresh_token") String refreshToken
    ) {
        TokenClaims claims = jwtUtil.parseRefreshToken(refreshToken);
        Long userId = claims.getUserId();

        if (FormatValidator.hasNoValue(userId)) {
            return new ResponseEntity<>(respondError(), HttpStatus.UNAUTHORIZED);
        }

        String redisKey = "refreshToken:" + userId;
        String expected = "r" + sha256Hex(refreshToken);
        String stored = stringRedisTemplate.opsForValue().get(redisKey);

        if (FormatValidator.notEquals(expected, stored)) {
            return new ResponseEntity<>(respondError(), HttpStatus.UNAUTHORIZED);
        }

        WebBasedTokenRefreshResponse response = authServiceAdapter.issueNewAccessToken(refreshToken);

        return new ResponseEntity<>(
                BaseResponse.of(
                        response.accessToken(),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "Access Token 재발급 성공"
                ),
                response.headers(),
                HttpStatus.CREATED
        );
    }

    private BaseResponse respondError() {
        return BaseResponse.of(
                null,
                HttpStatus.UNAUTHORIZED,
                FIRST_ERROR_CODE,
                "유효하지 않은 리프레시 토큰"
        );
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashAlgorithmNotAvailableException(e);
        }
    }

    /**
     * 이메일 인증 요청 전송
     *
     * @param email 이메일 주소
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 이메일 인증 요청을 전송합니다.
     */
    @SendEmailVerificationApiDocs
    @PostMapping("/email/verification")
    public ResponseEntity<BaseResponse<Void>> sendEmailVerification(
            @RequestParam("email")
            @NotEmpty(message = "이메일 주소는 필수값입니다.")
            @Pattern(regexp = EMAIL_PATTERN, message = INVALID_EMAIL_EXCEPTION_MESSAGE)
            String email
    ) {
        sendEmailVerificationUseCase.sendEmailVerification(email);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "이메일 인증 요청 전송 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 이메일 인증 코드 검증
     *
     * @param verifyCodeRequest 이메일 인증 코드 검증 요청
     * @Author 성효빈
     * @Date 2026-01-01
     * @Description 이메일 인증 코드를 검증합니다.
     */
    @PostMapping("/verification")
    @ValidateVerificationCodeApiDocs
    public ResponseEntity<BaseResponse<verifyCodeResponse>> verifyCode(
            @Valid @RequestBody VerifyCodeRequest verifyCodeRequest
    ) {
        VerifyCodeResult result = verifyCodeUseCase.verifyCode(
                UserRequestToCommandMapper.mapToCommand(verifyCodeRequest)
        );

        List<String> roleIds = List.of(result.roleId());
        Long id = result.id();
        String subject = String.valueOf(result.id());
        String accessToken = jwtUtil.generateAccessToken(subject, id, roleIds, AuthVendor.NATIVE);
        String refreshToken = jwtUtil.generateRefreshToken(subject, id, roleIds, AuthVendor.NATIVE);

        return new ResponseEntity<>(
                BaseResponse.of(
                        verifyCodeResponse.of(accessToken, refreshToken),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "이메일 인증 코드 검증 성공"
                ),
                HttpStatus.OK
        );
    }
}
