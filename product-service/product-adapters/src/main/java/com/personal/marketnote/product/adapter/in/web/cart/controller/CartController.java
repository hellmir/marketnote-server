package com.personal.marketnote.product.adapter.in.web.cart.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.product.adapter.in.web.cart.controller.apidocs.*;
import com.personal.marketnote.product.adapter.in.web.cart.mapper.CartRequestToCommandMapper;
import com.personal.marketnote.product.adapter.in.web.cart.request.AddCartProductRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductQuantityRequest;
import com.personal.marketnote.product.adapter.in.web.cart.response.GetMyCartProductsResponse;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
import com.personal.marketnote.product.port.in.usecase.cart.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 장바구니 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "장바구니 API", description = "장바구니 관련 API")
@RequiredArgsConstructor
public class CartController {
    private final AddCartProductUseCase addCartProductUseCase;
    private final GetCartProductUseCase getCartProductUseCase;
    private final UpdateCartProductQuantityUseCase updateCartProductQuantityUseCase;
    private final UpdateCartProductOptionsUseCase updateCartProductOptionsUseCase;
    private final DeleteCartProductUseCase deleteCartProductsUseCase;

    /**
     * 장바구니 상품 추가
     *
     * @param request   장바구니 상품 추가 요청
     * @param principal 인증 정보
     * @return 장바구니 상품 추가 응답 {@link Void}
     * @Author 성효빈
     * @Date 2026-01-04
     * @Description 장바구니에 상품을 추가합니다.
     */
    @PostMapping("/products")
    @AddCartProductApiDocs
    public ResponseEntity<BaseResponse<Void>> addCartProduct(
            @Valid @RequestBody AddCartProductRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        addCartProductUseCase.addCartProduct(
                CartRequestToCommandMapper.mapToCommand(ElementExtractor.extractUserId(principal), request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "장바구니 상품 추가 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 회원 장바구니 상품 목록 조회
     *
     * @param principal 인증 정보
     * @return 장바구니 상품 목록 {@link GetMyCartProductsResponse}
     * @Author 성효빈
     * @Date 2026-01-04
     * @Description 회원의 장바구니 상품 목록을 조회합니다.
     */
    @GetMapping("/products")
    @GetMyCartProductsApiDocs
    public ResponseEntity<BaseResponse<GetMyCartProductsResponse>> getMyCartProducts(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        GetMyCartProductsResult result = getCartProductUseCase.getMyCartProducts(
                ElementExtractor.extractUserId(principal)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetMyCartProductsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 장바구니 상품 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 장바구니 상품 수량 변경
     *
     * @param request   장바구니 상품 수량 변경 요청
     * @param principal 인증 정보
     * @return 장바구니 상품 수량 변경 응답 {@link Void}
     * @Author 성효빈
     * @Date 2026-01-04
     * @Description 장바구니 상품 수량을 변경합니다.
     */
    @PatchMapping("/products/quantity")
    @UpdateCartProductQuantityApiDocs
    public ResponseEntity<BaseResponse<Void>> updateCartProductQuantity(
            @Valid @RequestBody UpdateCartProductQuantityRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateCartProductQuantityUseCase.updateCartProductQuantity(
                CartRequestToCommandMapper.mapToCommand(ElementExtractor.extractUserId(principal), request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "장바구니 상품 수량 변경 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 장바구니 상품 옵션 변경
     *
     * @param request   장바구니 상품 옵션 변경 요청
     * @param principal 인증 정보
     * @param request   장바구니 상품 옵션 변경 요청
     * @param principal 인증 정보
     * @return 장바구니 상품 옵션 변경 응답 {@link Void}
     * @return 장바구니 상품 옵션 변경 응답 {@link Void}
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 장바구니 상품 옵션을 변경합니다.
     */
    @PatchMapping("/products/options")
    @UpdateCartProductOptionApiDocs
    public ResponseEntity<BaseResponse<Void>> updateCartProductOptions(
            @Valid @RequestBody UpdateCartProductOptionsRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateCartProductOptionsUseCase.updateCartProductOptions(
                CartRequestToCommandMapper.mapToCommand(ElementExtractor.extractUserId(principal), request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "장바구니 상품 옵션 변경 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 장바구니 상품 삭제
     *
     * @param pricePolicyIds 장바구니 상품 옵션 변경 요청
     * @param principal      인증 정보
     * @Author 성효빈
     * @Date 2026-01-04
     * @Description 장바구니 상품을 삭제합니다.
     */
    @DeleteMapping("/products")
    @DeleteCartProductApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteCartProducts(
            @Valid @RequestParam List<Long> pricePolicyIds,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteCartProductsUseCase.deleteCartProducts(
                CartRequestToCommandMapper.mapToCommand(ElementExtractor.extractUserId(principal), pricePolicyIds)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "장바구니 상품 삭제 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 장바구니 비우기
     *
     * @param principal 인증 정보
     * @Author 성효빈
     * @Date 2026-01-05
     * @Description 회원의 장바구니를 비웁니다.
     */
    @DeleteMapping
    @DeleteAllCartProductsApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteAllCartProducts(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteCartProductsUseCase.deleteAllCartProducts(ElementExtractor.extractUserId(principal));

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "장바구니 비우기 성공"
                ),
                HttpStatus.OK
        );
    }
}
