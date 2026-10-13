package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.port.out.aligo.GetAligoHeartInfoExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class AligoHeartInfoExternalAdapter implements GetAligoHeartInfoExternalPort {

    private static final String HEART_INFO_PATH = "/akv10/heartinfo/";

    private final AligoHttpClient aligoHttpClient;

    @Override
    public AligoHeartInfoResult getHeartInfo() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        AligoHeartInfoApiResponse response = aligoHttpClient.post(
                HEART_INFO_PATH, params, AligoHeartInfoApiResponse.class
        );

        return new AligoHeartInfoResult(
                response.smsCnt(), response.lmsCnt(), response.mmsCnt(), response.altCnt()
        );
    }
}
