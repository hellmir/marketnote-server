package com.personal.marketnote.reward.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OfferwallVendorCommunicationPayloadGeneratorTest {

    private OfferwallVendorCommunicationPayloadGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OfferwallVendorCommunicationPayloadGenerator(new ObjectMapper());
    }

    @Nested
    @DisplayName("buildResponsePayloadJson 메서드")
    class BuildResponsePayloadJsonTest {

        @Test
        @DisplayName("성공 응답이면 Result/ResultCode/ResultMsg 필드를 포함한 페이로드를 생성한다")
        void shouldBuildSuccessResponsePayload() {
            JsonNode result = generator.buildResponsePayloadJson(true, 200, "성공");

            assertThat(result.get("Result").asBoolean()).isTrue();
            assertThat(result.get("ResultCode").asInt()).isEqualTo(200);
            assertThat(result.get("ResultMsg").asText()).isEqualTo("성공");
        }

        @Test
        @DisplayName("실패 응답이면 Result는 false로 설정된다")
        void shouldBuildFailureResponsePayload() {
            JsonNode result = generator.buildResponsePayloadJson(false, 400, "실패");

            assertThat(result.get("Result").asBoolean()).isFalse();
            assertThat(result.get("ResultCode").asInt()).isEqualTo(400);
            assertThat(result.get("ResultMsg").asText()).isEqualTo("실패");
        }
    }

    @Nested
    @DisplayName("buildAdpopcornPayloadJson 메서드")
    class BuildAdpopcornPayloadJsonTest {

        @Test
        @DisplayName("모든 값이 채워지면 Adpopcorn 명세에 맞는 모든 필드가 포함된다")
        void shouldBuildAdpopcornPayloadWithAllFields() {
            JsonNode result = generator.buildAdpopcornPayloadJson(
                "rk-1",
                "user-1",
                UserDeviceType.ANDROID,
                "camp-1",
                1,
                "캠페인",
                100L,
                "sig",
                9999,
                "앱",
                "adid-1",
                "idfa-1",
                "2026-11-26T12:00:00"
            );

            assertThat(result.get("reward_key").asText()).isEqualTo("rk-1");
            assertThat(result.get("usn").asText()).isEqualTo("user-1");
            assertThat(result.get("user_device_type").asText()).isEqualTo("ANDROID");
            assertThat(result.get("campaign_key").asText()).isEqualTo("camp-1");
            assertThat(result.get("campaign_type").asText()).isEqualTo("1");
            assertThat(result.get("campaign_name").asText()).isEqualTo("캠페인");
            assertThat(result.get("quantity").asText()).isEqualTo("100");
            assertThat(result.get("signed_value").asText()).isEqualTo("sig");
            assertThat(result.get("app_key").asText()).isEqualTo("9999");
            assertThat(result.get("app_name").asText()).isEqualTo("앱");
            assertThat(result.get("adid").asText()).isEqualTo("adid-1");
            assertThat(result.get("idfa").asText()).isEqualTo("idfa-1");
            assertThat(result.get("time_stamp").asText()).isEqualTo("2026-11-26T12:00:00");
        }

        @Test
        @DisplayName("문자열이 null이면 해당 필드는 생략된다")
        void shouldOmitNullStringFields() {
            JsonNode result = generator.buildAdpopcornPayloadJson(
                null,
                "user-1",
                UserDeviceType.IOS,
                null,
                1,
                null,
                100L,
                null,
                9999,
                null,
                null,
                null,
                null
            );

            assertThat(result.has("reward_key")).isFalse();
            assertThat(result.has("campaign_key")).isFalse();
            assertThat(result.has("campaign_name")).isFalse();
            assertThat(result.has("signed_value")).isFalse();
            assertThat(result.has("app_name")).isFalse();
            assertThat(result.has("adid")).isFalse();
            assertThat(result.has("idfa")).isFalse();
            assertThat(result.has("time_stamp")).isFalse();
            assertThat(result.get("usn").asText()).isEqualTo("user-1");
            assertThat(result.get("user_device_type").asText()).isEqualTo("IOS");
        }

        @Test
        @DisplayName("문자열이 빈 값이면 해당 필드는 생략된다")
        void shouldOmitBlankStringFields() {
            JsonNode result = generator.buildAdpopcornPayloadJson(
                "  ",
                "",
                UserDeviceType.ANDROID,
                "",
                null,
                "",
                null,
                "",
                null,
                "",
                "",
                "",
                ""
            );

            assertThat(result.has("reward_key")).isFalse();
            assertThat(result.has("usn")).isFalse();
            assertThat(result.has("campaign_key")).isFalse();
            assertThat(result.has("campaign_type")).isFalse();
            assertThat(result.has("campaign_name")).isFalse();
            assertThat(result.has("quantity")).isFalse();
            assertThat(result.has("signed_value")).isFalse();
            assertThat(result.has("app_key")).isFalse();
            assertThat(result.has("app_name")).isFalse();
            assertThat(result.has("adid")).isFalse();
            assertThat(result.has("idfa")).isFalse();
            assertThat(result.has("time_stamp")).isFalse();
            assertThat(result.get("user_device_type").asText()).isEqualTo("ANDROID");
        }

        @Test
        @DisplayName("Number 타입이 null이면 해당 필드는 생략된다")
        void shouldOmitNullNumberFields() {
            JsonNode result = generator.buildAdpopcornPayloadJson(
                "rk",
                "user",
                UserDeviceType.ANDROID,
                "camp",
                null,
                "name",
                null,
                "sig",
                null,
                "app",
                "adid",
                "idfa",
                "ts"
            );

            assertThat(result.has("campaign_type")).isFalse();
            assertThat(result.has("quantity")).isFalse();
            assertThat(result.has("app_key")).isFalse();
        }
    }

    @Nested
    @DisplayName("buildTnkPayloadJson 메서드")
    class BuildTnkPayloadJsonTest {

        @Test
        @DisplayName("모든 값이 채워지면 Tnk 명세에 맞는 모든 필드가 포함된다")
        void shouldBuildTnkPayloadWithAllFields() {
            JsonNode result = generator.buildTnkPayloadJson(
                "seq-1",
                "user-1",
                UserDeviceType.ANDROID,
                2,
                300L,
                "chk",
                "앱",
                "app-id",
                "2026-11-26",
                new BigDecimal("1500.50")
            );

            assertThat(result.get("seq_id").asText()).isEqualTo("seq-1");
            assertThat(result.get("md_user_nm").asText()).isEqualTo("user-1");
            assertThat(result.get("user_device_type").asText()).isEqualTo("ANDROID");
            assertThat(result.get("campaign_type").asText()).isEqualTo("2");
            assertThat(result.get("pay_pnt").asText()).isEqualTo("300");
            assertThat(result.get("md_chk").asText()).isEqualTo("chk");
            assertThat(result.get("app_nm").asText()).isEqualTo("앱");
            assertThat(result.get("app_id").asText()).isEqualTo("app-id");
            assertThat(result.get("pay_dt").asText()).isEqualTo("2026-11-26");
            assertThat(result.get("pay_amt").asText()).isEqualTo("1500.50");
        }

        @Test
        @DisplayName("Number 타입이 null이면 해당 필드는 생략된다")
        void shouldOmitNullNumberFieldsForTnk() {
            JsonNode result = generator.buildTnkPayloadJson(
                "seq",
                "user",
                UserDeviceType.IOS,
                null,
                null,
                "chk",
                "앱",
                "app-id",
                "ts",
                null
            );

            assertThat(result.has("campaign_type")).isFalse();
            assertThat(result.has("pay_pnt")).isFalse();
            assertThat(result.has("pay_amt")).isFalse();
            assertThat(result.get("user_device_type").asText()).isEqualTo("IOS");
        }
    }

    @Nested
    @DisplayName("buildAdiscopePayloadJson 메서드")
    class BuildAdiscopePayloadJsonTest {

        @Test
        @DisplayName("모든 값이 채워지면 Adiscope 명세에 맞는 모든 필드가 포함된다")
        void shouldBuildAdiscopePayloadWithAllFields() {
            JsonNode result = generator.buildAdiscopePayloadJson(
                "tx-1",
                "user-1",
                UserDeviceType.ANDROID,
                "unit-1",
                "VIDEO",
                "광고",
                "POINT",
                500L,
                "sig",
                "adid-1",
                "AdNetwork"
            );

            assertThat(result.get("transactionId").asText()).isEqualTo("tx-1");
            assertThat(result.get("userId").asText()).isEqualTo("user-1");
            assertThat(result.get("userDeviceType").asText()).isEqualTo("ANDROID");
            assertThat(result.get("unitId").asText()).isEqualTo("unit-1");
            assertThat(result.get("shareAdType").asText()).isEqualTo("VIDEO");
            assertThat(result.get("adname").asText()).isEqualTo("광고");
            assertThat(result.get("rewardUnit").asText()).isEqualTo("POINT");
            assertThat(result.get("rewardAmount").asText()).isEqualTo("500");
            assertThat(result.get("signature").asText()).isEqualTo("sig");
            assertThat(result.get("adid").asText()).isEqualTo("adid-1");
            assertThat(result.get("network").asText()).isEqualTo("AdNetwork");
        }

        @Test
        @DisplayName("Number 타입과 문자열이 null이면 해당 필드는 생략된다")
        void shouldOmitNullFieldsForAdiscope() {
            JsonNode result = generator.buildAdiscopePayloadJson(
                null,
                "user",
                UserDeviceType.IOS,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            assertThat(result.has("transactionId")).isFalse();
            assertThat(result.has("unitId")).isFalse();
            assertThat(result.has("shareAdType")).isFalse();
            assertThat(result.has("adname")).isFalse();
            assertThat(result.has("rewardUnit")).isFalse();
            assertThat(result.has("rewardAmount")).isFalse();
            assertThat(result.has("signature")).isFalse();
            assertThat(result.has("adid")).isFalse();
            assertThat(result.has("network")).isFalse();
            assertThat(result.get("userId").asText()).isEqualTo("user");
            assertThat(result.get("userDeviceType").asText()).isEqualTo("IOS");
        }
    }
}
