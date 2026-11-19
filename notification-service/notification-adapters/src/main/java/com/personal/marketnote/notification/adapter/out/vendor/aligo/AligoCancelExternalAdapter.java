package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.port.out.aligo.CancelAligoMessageExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class AligoCancelExternalAdapter implements CancelAligoMessageExternalPort {

    private static final String CANCEL_PATH = "/akv10/cancel/";
    private static final String CANCEL_MESSAGE_OPERATION = "cancelMessage";

    private final AligoHttpClient aligoHttpClient;

    @Override
    public void cancel(String mid) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mid", mid);

        aligoHttpClient.post(CANCEL_MESSAGE_OPERATION, CANCEL_PATH, params, AligoApiResponse.class);
    }
}
