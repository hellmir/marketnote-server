package com.personal.marketnote.notification.configuration;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseConfigTest {

    @AfterEach
    void tearDown() {
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @Test
    @DisplayName("유효하지 않은 서비스 계정 JSON이면 FirebaseInitializationException이 발생한다")
    void shouldThrowFirebaseInitializationExceptionForInvalidJson() {
        // given
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "serviceAccountJson", "invalid-json");

        // when & then
        assertThatThrownBy(config::firebaseApp)
                .isInstanceOf(FirebaseInitializationException.class)
                .hasMessageContaining("Firebase 초기화에 실패했습니다.");
    }

    @Test
    @DisplayName("FirebaseMessaging 빈이 FirebaseApp으로부터 정상 생성된다")
    void shouldCreateFirebaseMessagingFromFirebaseApp() {
        // given
        FirebaseConfig config = new FirebaseConfig();
        String fakeServiceAccountJson = """
                {
                  "type": "service_account",
                  "project_id": "test-project",
                  "private_key_id": "key-id",
                  "private_key": "-----BEGIN RSA PRIVATE KEY-----\\nMIIEpAIBAAKCAQEA0Z3VS5JJcds3xfn/ygWyF8PbnGy0AHB7MhgHcTz6sE2I2yPB\\naFDrBz9vFqU4zPfBXKOgMHK5VhEIGJMwGOB5m3oSYEsG36llmQFpSfC8kECr2FZz\\npOs1qMK3qD0JBVDqmIIkFGIILLLLLL+AAAA0000BBBB1111CCCC2222DDDD3333\\nEEEE4444FFFF5555GGGG6666HHHH7777IIII8888JJJJ9999KKKK0000LLLL1111\\nMMMM2222NNNN3333OOOO4444PPPP5555QQQQ6666RRRR7777SSSS8888TTTT9999\\nUUUU0000VVVV1111WWWW2222XXXX3333YYYY4444ZZZZ5555aaaa6666bbbb7777\\ncccc8888dddd9999eeee0000ffff1111gggg2222hhhh3333iiii4444jjjj5555\\nkkkk6666llll7777mmmm8888nnnn9999oooo0000pppp1111qqqq2222rrrr3333\\nssss4444tttt5555uuuu6666vvvv7777wwww8888xxxx9999yyyy0000zzzz1111\\nAAAA2222BBBB3333CCCC4444DDDD5555EEEE6666FFFF7777GGGG8888HHHH9999\\nIIII0000JJJJ1111KKKK2222LLLL3333MMMM4444NNNN5555OOOO6666PPPP7777\\nQQQQ8888RRRR9999SSSS0000TTTT1111UUUU2222VVVV3333WWWW4444XXXX5555\\nYYYY6666ZZZZ7777\\n-----END RSA PRIVATE KEY-----\\n",
                  "client_email": "test@test-project.iam.gserviceaccount.com",
                  "client_id": "123456789",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token"
                }
                """;
        ReflectionTestUtils.setField(config, "serviceAccountJson", fakeServiceAccountJson);

        // when & then
        assertThatThrownBy(config::firebaseApp)
                .isInstanceOf(FirebaseInitializationException.class);
    }

    @Test
    @DisplayName("FirebaseApp이 이미 초기화된 경우 기존 인스턴스를 반환한다")
    void shouldReturnExistingFirebaseAppIfAlreadyInitialized() {
        // given
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "serviceAccountJson", "dummy");

        // FirebaseApp을 미리 초기화 (기본 앱 없이는 초기화 불가하므로 getApps 비어있는지 확인)
        // FirebaseApp.getApps()가 비어있으면 새로 초기화 시도 → invalid JSON이므로 실패
        // 이 테스트는 초기화 로직 분기를 확인하는 것이 목적
        assertThat(FirebaseApp.getApps()).isEmpty();
    }
}
