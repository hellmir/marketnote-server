package com.personal.marketnote.product.adapter.in.web.pricepolicy.response;

import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GetPricePoliciesResponse {
    private List<PricePolicyItemResponse> policies;

    public static GetPricePoliciesResponse from(GetPricePoliciesResult result) {
        return GetPricePoliciesResponse.builder()
                .policies(result.policies().stream().map(PricePolicyItemResponse::from).toList())
                .build();
    }
}


