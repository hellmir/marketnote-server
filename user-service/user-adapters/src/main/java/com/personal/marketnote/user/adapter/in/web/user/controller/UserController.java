package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.adapter.in.web.user.controller.apidocs.*;
import com.personal.marketnote.user.adapter.in.web.user.mapper.UserRequestToCommandMapper;
import com.personal.marketnote.user.adapter.in.web.user.request.SignInRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.SignUpRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.*;
import com.personal.marketnote.user.port.in.result.SignInResult;
import com.personal.marketnote.user.port.in.result.SignUpResult;
import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.port.in.usecase.user.*;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;

// TODO: 로그인 내역 기능 추가

/**
 * 회원 컨트롤러
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 회원 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "회원 API", description = "회원 관련 API")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final RegisterReferredUserCodeUseCase registerReferredUserCodeUseCase;
    private final SignOutUseCase signOutUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.jwt.refresh-token.ttl:1209600000}")
    private Long refreshTokenTtlMillis;

    /**
     * 회원 가입
     *
     * @param signUpRequest 회원 가입 요청
     * @param principal     OAuth2 인증 정보
     * @return 인증 토큰 응답 {@link AuthenticationTokenResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 회원으로 가입합니다.
     */
    @PostMapping("/sign-up")
    @SignUpApiDocs
    public ResponseEntity<BaseResponse<SignUpResponse>> signUpUser(
            @Valid @RequestBody SignUpRequest signUpRequest,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            HttpServletRequest request
    ) {
        AuthVendor authVendor = AuthVendor.NATIVE;
        String oidcId = null;

        if (FormatValidator.hasValue(principal)) {
            oidcId = principal.getAttribute(SUB_CLAIM_KEY);
            String issuer = principal.getAttribute(ISS_CLAIM_KEY);
            authVendor = resolveVendorFromIssuer(FormatConverter.toUpperCase(issuer));
        }

        SignUpResult signUpResult = signUpUseCase.signUp(
                UserRequestToCommandMapper.mapToCommand(signUpRequest), authVendor, oidcId, extractClientIp(request)
        );

        List<String> roleIds = List.of(signUpResult.roleId());
        Long id = signUpResult.id();
        String subject = String.valueOf(signUpResult.id());
        String accessToken = jwtUtil.generateAccessToken(subject, id, roleIds, authVendor);
        String refreshToken = jwtUtil.generateRefreshToken(subject, id, roleIds, authVendor);

        String redisKey = "userId:" + id;
        String refreshTokenValue = "r" + encodeBySha256Hex(refreshToken);
        stringRedisTemplate.opsForValue()
                .set(Objects.requireNonNull(redisKey), Objects.requireNonNull(refreshTokenValue),
                        Objects.requireNonNull(refreshTokenTtlMillis), TimeUnit.MILLISECONDS);

        HttpStatus httpStatus = HttpStatus.CREATED;
        boolean isNewUser = signUpResult.isNewUser();
        if (!isNewUser) {
            httpStatus = HttpStatus.OK;
        }

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .status(httpStatus)
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(
                        BaseResponse.of(
                                SignUpResponse.of(accessToken, isNewUser),
                                httpStatus,
                                DEFAULT_SUCCESS_CODE,
                                "회원 가입 성공"
                        )
                );
    }

    /**
     * 추천 회원 초대 코드 등록
     *
     * @param referredUserCode 추천 회원의 초대 코드
     * @param principal        사용자 인증 정보
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 자신을 추천한 회원의 초대 코드를 등록합니다.
     */
    @PatchMapping("/referred-user-code")
    @RegisterReferredUserCodeApiDocs
    public ResponseEntity<BaseResponse<Void>> registerReferredUserCode(
            @Valid @RequestParam String referredUserCode,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        registerReferredUserCodeUseCase.registerReferredUserCode(
                ElementExtractor.extractUserId(principal), referredUserCode
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "초대 코드 등록 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 회원 로그인
     *
     * @param signInRequest 로그인 요청
     * @param principal     사용자 인증 정보
     * @return 인증 토큰 응답 {@link AuthenticationTokenResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 회원 로그인을 수행합니다.
     */
    @PostMapping("/sign-in")
    @SignInApiDocs
    public ResponseEntity<BaseResponse<SignInResponse>> signInUser(
            @Valid @RequestBody SignInRequest signInRequest,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            HttpServletRequest request
    ) {
        AuthVendor authVendor = AuthVendor.NATIVE;
        String oidcId = null;

        if (FormatValidator.hasValue(principal)) {
            oidcId = principal.getAttribute(SUB_CLAIM_KEY);
            String issuer = principal.getAttribute(ISS_CLAIM_KEY);
            authVendor = resolveVendorFromIssuer(FormatConverter.toUpperCase(issuer));
        }

        String clientIp = extractClientIp(request);

        SignInResult signInResult = signInUseCase.signIn(
                UserRequestToCommandMapper.mapToCommand(signInRequest), authVendor, oidcId, clientIp
        );

        List<String> roleIds = List.of(signInResult.roleId());
        Long id = signInResult.id();
        String subject = String.valueOf(signInResult.id());
        String accessToken = jwtUtil.generateAccessToken(subject, id, roleIds, authVendor);
        String refreshToken = jwtUtil.generateRefreshToken(subject, id, roleIds, authVendor);

        String redisKey = "refreshToken:" + id;
        String refreshTokenValue = "r" + encodeBySha256Hex(refreshToken);
        stringRedisTemplate.opsForValue()
                .set(Objects.requireNonNull(redisKey), Objects.requireNonNull(refreshTokenValue),
                        Objects.requireNonNull(refreshTokenTtlMillis), TimeUnit.MILLISECONDS);

        HttpStatus httpStatus = HttpStatus.OK;

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .status(httpStatus)
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(
                        BaseResponse.of(
                                SignInResponse.of(accessToken, signInResult.isRequiredTermsAgreed()),
                                HttpStatus.OK,
                                DEFAULT_SUCCESS_CODE,
                                "회원 로그인 성공"
                        )
                );
    }

    private String encodeBySha256Hex(String input) {
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
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (FormatValidator.hasValue(xff)) {
            return xff.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (FormatValidator.hasValue(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    private AuthVendor resolveVendorFromIssuer(String issuer) {
        if (issuer.contains(AuthVendor.KAKAO.name())) {
            return AuthVendor.KAKAO;
        }

        if (issuer.contains(AuthVendor.GOOGLE.name())) {
            return AuthVendor.GOOGLE;
        }

        if (issuer.contains(AuthVendor.APPLE.name())) {
            return AuthVendor.APPLE;
        }

        return AuthVendor.NATIVE;
    }

    /**
     * 자신의 회원 키 조회
     *
     * @param principal 사용자 인증 정보
     * @return 회원 키 조회 응답 {@link GetUserKeyResponse}
     * @Author 성효빈
     * @Date 2026-01-19
     * @Description 자신의 회원 키를 조회합니다.
     */
    @GetMapping("/my-key")
    @GetMyKeyApiDocs
    public ResponseEntity<BaseResponse<GetUserKeyResponse>> getMyKey(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetUserKeyResponse getUserKeyResponse = GetUserKeyResponse.of(
                getUserUseCase.getUserKey(ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        getUserKeyResponse,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "자신의 회원 키 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 자신의 정보 조회
     *
     * @param principal 사용자 인증 정보
     * @return 회원 정보 조회 응답 {@link GetUserInfoResponse}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 자신의 정보를 조회합니다.
     */
    @GetMapping("/me")
    @GetMyInfoApiDocs
    public ResponseEntity<BaseResponse<GetUserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetUserInfoResponse getUserInfoResponse = GetUserInfoResponse.from(
                getUserUseCase.getUserInfo(ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        getUserInfoResponse,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "자신의 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 자신의 정보 수정
     *
     * @param updateUserInfoRequest 자신의 정보 수정 요청
     * @param principal             사용자 인증 정보
     * @return 회원 정보 수정 응답 {@link Void}
     * @Author 성효빈
     * @Date 2025-12-27
     * @Description 회원 정보를 수정합니다.
     */
    @PatchMapping
    @UpdateMyInfoApiDocs
    public ResponseEntity<BaseResponse<Void>> updateMyInfo(
            @Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateUserUseCase.updateUserInfo(
                false,
                ElementExtractor.extractUserId(principal),
                UserRequestToCommandMapper.mapToCommand(updateUserInfoRequest)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "자신의 정보 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 회원 로그아웃
     *
     * @param refreshToken(cookie) Refresh Token; HTTP-only
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 로그아웃합니다.
     */
    @DeleteMapping("/sign-out")
    @SignOutApiDocs
    public ResponseEntity<BaseResponse<Void>> signOutUser(@CookieValue(value = "refresh_token") String refreshToken) {
        HttpHeaders httpHeaders = signOutUseCase.signOut(refreshToken);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 로그아웃 성공"
                ),
                httpHeaders,
                HttpStatus.OK
        );
    }

    /**
     * 회원 탈퇴
     *
     * @param principal         사용자 인증 정보
     * @param googleAccessToken Google 액세스 토큰
     * @return 회원 탈퇴 응답 {@link WithdrawResponse}
     * @Author 성효빈
     * @Date 2025-12-29
     * @Description 회원 탈퇴를 수행합니다.
     */
    @DeleteMapping
    @WithdrawalApiDocs
    public ResponseEntity<BaseResponse<WithdrawResponse>> withdrawUser(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestHeader(value = "X-Google-Access-Token", required = false) String googleAccessToken
    ) {
        WithdrawResult result = withdrawUseCase.withdrawUser(
                ElementExtractor.extractUserId(principal), googleAccessToken
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        WithdrawResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 탈퇴 성공"
                ),
                HttpStatus.OK
        );
    }
}
