package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.GetProductSearchTargetsResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetProductSearchTargetsResponse {
    private List<ProductSearchTargetItemResponse> targets;

    public static GetProductSearchTargetsResponse from(GetProductSearchTargetsResult result) {
        return new GetProductSearchTargetsResponse(Arrays.stream(result.targets())
                .map(ProductSearchTargetItemResponse::from)
                .toList());
    }
}


