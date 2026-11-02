package com.personal.marketnote.product.adapter.in.web.pricepolicy.response;

import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RegisterPricePolicyResponse {
    private Long id;

    public static RegisterPricePolicyResponse from(RegisterPricePolicyResult result) {
        return RegisterPricePolicyResponse.builder()
                .id(result.id())
                .build();
    }
}


