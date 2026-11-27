package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.port.out.aligo.CancelAligoMessageExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class AligoCancelExternalAdapter implements CancelAligoMessageExternalPort {

    private static final String CANCEL_MESSAGE_OPERATION = "cancelMessage";

    private final AligoHttpClient aligoHttpClient;
    private final AligoProperties aligoProperties;

    @Override
    public void cancel(String mid) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mid", mid);

        aligoHttpClient.post(
            CANCEL_MESSAGE_OPERATION, aligoProperties.getApi().getCancelPath(),
            params, AligoApiResponse.class
        );
    }
}
