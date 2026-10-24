package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.GetAligoSenderProfileListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoSenderProfileExternalPort;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoSenderAuthenticationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AligoSenderProfileExternalAdapter
        implements RequestAligoSenderAuthenticationPort, RegisterAligoSenderProfileExternalPort,
        GetAligoSenderProfileListExternalPort {

    private final AligoHttpClient aligoHttpClient;

    @Override
    public String requestAuthentication(String plusId, String phoneNumber) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("plusid", plusId);
        params.add("phonenumber", phoneNumber);

        AligoAuthResponse response = aligoHttpClient.post("/akv10/profile/auth/", params, AligoAuthResponse.class);
        return response.senderKey();
    }

    @Override
    public void registerProfile(String senderKey, String categoryCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", senderKey);
        params.add("categorycode", categoryCode);

        aligoHttpClient.post("/akv10/profile/add/", params, AligoApiResponse.class);
    }

    @Override
    public List<AligoSenderProfile> getProfileList() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        AligoProfileListResponse response = aligoHttpClient.post(
                "/akv10/profile/list/", params, AligoProfileListResponse.class
        );

        if (FormatValidator.hasNoValue(response.list())) {
            return List.of();
        }

        return response.list().stream()
                .map(this::toDomain)
                .toList();
    }

    private AligoSenderProfile toDomain(AligoProfileListResponse.ProfileItem item) {
        return AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey(item.senderKey())
                        .plusId(item.plusId())
                        .name(item.name())
                        .categoryCode(item.categoryCode())
                        .profileInspectionStatus(AligoProfileInspectionStatus.from(item.profileInspectionStatus()))
                        .senderProfileStatus(AligoSenderProfileStatus.from(item.senderProfileStatus()))
                        .alimUseYn("Y".equals(item.alimUseYn()))
                        .uuid(item.uuid())
                        .build()
        );
    }

    record AligoAuthResponse(
            @JsonProperty("result_code") int resultCode,
            String message,
            @JsonProperty("senderkey") String senderKey
    ) {
    }

    record AligoProfileListResponse(
            @JsonProperty("result_code") int resultCode,
            String message,
            List<ProfileItem> list
    ) {
        record ProfileItem(
                @JsonProperty("senderkey") String senderKey,
                @JsonProperty("plusid") String plusId,
                String name,
                @JsonProperty("categorycode") String categoryCode,
                @JsonProperty("profileInspectionStatus") String profileInspectionStatus,
                @JsonProperty("senderProfileStatus") String senderProfileStatus,
                @JsonProperty("alim_use_yn") String alimUseYn,
                String uuid
        ) {
        }
    }
}
