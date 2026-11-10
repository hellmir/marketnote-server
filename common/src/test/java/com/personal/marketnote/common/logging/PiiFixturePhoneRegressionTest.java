package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 fixture 전화번호/실성명 회귀 방지 테스트.
 *
 * <p>배경: {@code PiiMaskingConverter}는 test profile에서 {@code %msg} 평문 출력을 허용한다.
 * CI 아티팩트에 테스트 로그가 적재될 때 실제 사용자 PII(전화번호, 실성명)가 fixture에 포함되어 있으면
 * 유출 경로가 된다. {@link PiiFixtureRegressionTest}가 이메일 도메인을 스캔하듯이 전화번호도
 * 화이트리스트 기반으로 스캔한다.
 *
 * <p>규칙: 프로젝트 전체 {@code *Test.java}(빌드 산출물/로컬 히스토리 제외) 파일에서 한국 휴대전화
 * prefix({@code 010}, {@code 011}, {@code 016}, {@code 017}, {@code 018}, {@code 019})와 유선번호
 * prefix({@code 02}, {@code 031~033}, {@code 041~044}, {@code 051~055}, {@code 061~064})
 * 및 인터넷전화({@code 070})로 시작하는 패턴을 추출하여 하이픈 제거 후 9~11자리로 정규화한다.
 * 서울 유선({@code 02})은 본문 7~8자리로 9~10자리, 모바일/지역 유선/070은 10~11자리이다.
 * 정규화된 값이 {@link #DUMMY_PHONE_WHITELIST}에 포함되지 않으면 회귀 실패로 처리하여
 * 실PII 유입을 차단한다. 위반 리포트에는 일치한 번호의 뒷 4자리를 {@code *}로 마스킹하여
 * 리포트 자체가 유출 경로가 되지 않도록 한다.
 *
 * <p>실성명: 한국어 2~3글자 성명 패턴은 Java import/클래스명/변수명과 false positive 중첩이 크기
 * 때문에 현 시점에서는 허용 성명 {@link #ALLOWED_DUMMY_NAMES} 상수만 정의하여 fixture 작성 가이드로
 * 남긴다. 적극 스캔은 후속 이슈에서 재검토한다.
 *
 * <p>재현 쉘 명령:
 * <pre>{@code
 * grep -rhoE --include="*Test.java" \
 *     '(^|[^0-9])(01[016789]|02|03[1-3]|04[1-4]|05[1-5]|06[1-4]|070)-?[0-9]{3,4}-?[0-9]{4}([^0-9]|$)' . \
 *     | grep -v '/build/' | grep -v '/.history/' \
 *     | grep -oE '(01[016789]|02|03[1-3]|04[1-4]|05[1-5]|06[1-4]|070)-?[0-9]{3,4}-?[0-9]{4}' | tr -d '-' \
 *     | awk 'length($0)>=9 && length($0)<=11' | sort -u
 * }</pre>
 */
class PiiFixturePhoneRegressionTest {

    /**
     * 테스트 공용 더미 전화번호(정규화된 9~11자리). 현재 fixture 초기 감사 결과 기반으로 구성.
     *
     * <p>추가 시 원칙:
     * <ul>
     *   <li>반복 자릿수(예: {@code 01011111111}, {@code 01000000000})</li>
     *   <li>순차/공용 시퀀스(예: {@code 01012345678}, {@code 01098765432})</li>
     *   <li>테스트 데이터 시드로 명백한 패턴(예: {@code 010xxxx0001})</li>
     *   <li>10자리 canonical 샘플(예: {@code 0101234567}) — 전화번호 검증 테스트의 성공 샘플</li>
     *   <li>011/016/017/018/019 prefix canonical 샘플 — 이통사 통합 이후 DB 잔존 및 검증 테스트용</li>
     *   <li>유선번호 canonical 샘플(02/031~064/070) — 배송지/주문/풀필먼트 fixture 검증용</li>
     * </ul>
     * 실제 유통 가능성이 있는 번호(예: 임직원 사번 연관/실 사용자 DB 유출 번호)는 절대 추가 금지.
     */
    private static final Set<String> DUMMY_PHONE_WHITELIST = Set.of(
            "01000000000",
            "01000010001",
            "01000010002",
            "01000020001",
            "01000030001",
            "01000040001",
            "01000040002",
            "01000050001",
            "01000060001",
            "01000070001",
            "01000070002",
            "01000080001",
            "01000090001",
            "01000090002",
            "01000100001",
            "01000100002",
            "01000110001",
            "01000110002",
            "01010000001",
            "01010000002",
            "01011111111",
            "01011112221",
            "01011112222",
            "01011113333",
            "01011114444",
            "01011115555",
            "01012345678",
            "01012345679",
            "01020000001",
            "01020000002",
            "01020000003",
            "01021000001",
            "01021000002",
            "01022000001",
            "01022222222",
            "01022223333",
            "01023000001",
            "01030000001",
            "01030000002",
            "01033333333",
            "01033334444",
            "01044444444",
            "01055555555",
            "01055556666",
            "01066666666",
            "01077778888",
            "01087654321",
            "01098765432",
            "01099990001",
            "01099998888",
            "0101234000",
            "0101234567",
            "0109000000",
            "0111234567",
            "0161234567",
            "0171234567",
            "0181234567",
            "0191234567",
            "01112345678",
            "01612345678",
            "01712345678",
            "01812345678",
            "01912345678",
            "021234567",
            "0212345678",
            "0212345679",
            "027654321",
            "0311234567",
            "03112345678",
            "0321234567",
            "03212345678",
            "0331234567",
            "03312345678",
            "0411234567",
            "04112345678",
            "0421234567",
            "04212345678",
            "0431234567",
            "04312345678",
            "0441234567",
            "04412345678",
            "0511234567",
            "05112345678",
            "0521234567",
            "05212345678",
            "0531234567",
            "05312345678",
            "0541234567",
            "05412345678",
            "0551234567",
            "05512345678",
            "0611234567",
            "06112345678",
            "0621234567",
            "06212345678",
            "0631234567",
            "06312345678",
            "0641234567",
            "06412345678",
            "0701234567",
            "07012345678"
    );

    /**
     * 테스트 공용 허용 가공 성명 화이트리스트.
     *
     * <p>현 단계에서는 스캔 로직을 도입하지 않고 fixture 작성 가이드 상수로만 정의한다. 한국어
     * 2~3글자 이름은 변수명/JPA 필드/import 경로 등과 false positive가 크기 때문. 후속 이슈에서
     * 적극 스캔 도입 여부를 재검토한다.
     *
     * <p>{@code 테스트}/{@code test} prefix(예: {@code 테스트유저01}, {@code testuser}) 또한 허용되며
     * 상수 집합으로 열거하지 않는다.
     */
    @SuppressWarnings("unused")
    private static final Set<String> ALLOWED_DUMMY_NAMES = Set.of(
            "홍길동",
            "김철수",
            "이영희",
            "박영수",
            "최민수"
    );

    /**
     * 앞뒤 숫자 경계를 확인하여 타임스탬프/시퀀스 리터럴 오탐을 차단한다. 허용 prefix는 다음과 같이
     * {@link com.personal.marketnote.common.utility.RegularExpressionConstant}의
     * {@code PHONE_NUMBER_PATTERN}과 정합한다.
     * <ul>
     *   <li>모바일: {@code 010/011/016/017/018/019} (10~11자리)</li>
     *   <li>서울 유선: {@code 02} (9~10자리)</li>
     *   <li>지역 유선: {@code 031~033}, {@code 041~044}, {@code 051~055}, {@code 061~064} (10~11자리)</li>
     *   <li>인터넷전화: {@code 070} (10~11자리)</li>
     * </ul>
     */
    static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(01[016789]|02|03[1-3]|04[1-4]|05[1-5]|06[1-4]|070)-?\\d{3,4}-?\\d{4}(?!\\d)"
    );

    private static final List<String> EXCLUDED_PATH_SEGMENTS = List.of(
            "/build/",
            "/.history/",
            "/.git/",
            "/.gradle/",
            "/bin/"
    );

    @Test
    @DisplayName("테스트 소스에 더미 화이트리스트 외 전화번호가 하드코딩되어 있으면 실패한다")
    void shouldNotContainNonWhitelistedPhoneNumberInTestSources() throws IOException {
        Path projectRoot = resolveProjectRoot();
        List<String> violations = new ArrayList<>();
        long scannedCount;

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            scannedCount = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Test.java"))
                    .filter(path -> !isExcluded(path))
                    .peek(path -> collectViolations(path, projectRoot, violations))
                    .count();
        }

        assertThat(scannedCount)
                .as(
                        "프로젝트 루트 탐색 실패로 스캔된 *Test.java가 0건입니다. "
                                + "회귀 방지 장치가 침묵 실패했습니다. resolveProjectRoot()를 확인하십시오. root=%s",
                        projectRoot
                )
                .isGreaterThan(0);

        assertThat(violations)
                .as(
                        "테스트 fixture에 더미 화이트리스트 외 전화번호가 하드코딩되어 있습니다. "
                                + "CI 로그 아티팩트 유출 방지를 위해 %s 중 하나로 교체하거나 "
                                + "새 더미 번호를 DUMMY_PHONE_WHITELIST에 추가하십시오. 위반 목록:%n%s",
                        DUMMY_PHONE_WHITELIST,
                        String.join(System.lineSeparator(), violations)
                )
                .isEmpty();
    }

    private Path resolveProjectRoot() {
        Path workingDir = Paths.get("").toAbsolutePath().normalize();
        Path parent = workingDir.getParent();
        if (parent != null && Files.exists(parent.resolve("settings.gradle.kts"))) {
            return parent;
        }
        if (Files.exists(workingDir.resolve("settings.gradle.kts"))) {
            return workingDir;
        }
        return workingDir;
    }

    private boolean isExcluded(Path path) {
        String normalized = path.toString().replace('\\', '/');
        if (normalized.endsWith("/PiiFixturePhoneRegressionTest.java")) {
            return true;
        }
        for (String segment : EXCLUDED_PATH_SEGMENTS) {
            if (normalized.contains(segment)) {
                return true;
            }
        }
        return false;
    }

    private void collectViolations(Path path, Path projectRoot, List<String> violations) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException ioe) {
            return;
        }
        Path relative = projectRoot.relativize(path);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher matcher = PHONE_PATTERN.matcher(line);
            while (matcher.find()) {
                String matched = matcher.group();
                String normalized = matched.replace("-", "");
                if (normalized.length() < 9 || normalized.length() > 11) {
                    continue;
                }
                if (DUMMY_PHONE_WHITELIST.contains(normalized)) {
                    continue;
                }
                violations.add(
                        relative + ":" + (index + 1) + " -> " + maskLastFourDigits(matched)
                                + " (normalized=" + maskNormalizedLastFourDigits(normalized) + ")"
                );
            }
        }
    }

    /**
     * 일치한 원문 전화번호의 뒷 4자리 숫자만 {@code *}로 치환하여 반환한다. 하이픈은 보존한다.
     *
     * <p>예: {@code 010-1234-5678} → {@code 010-1234-****}, {@code 0101234567} → {@code 010123****}.
     */
    private static String maskLastFourDigits(String matched) {
        char[] chars = matched.toCharArray();
        int remaining = 4;
        for (int i = chars.length - 1; i >= 0 && remaining > 0; i--) {
            if (Character.isDigit(chars[i])) {
                chars[i] = '*';
                remaining--;
            }
        }
        return new String(chars);
    }

    /**
     * 정규화된(하이픈 제거) 전화번호의 뒷 4자리를 {@code *}로 치환하여 반환한다.
     *
     * <p>예: {@code 01012345678} → {@code 0101234****}, {@code 0101234567} → {@code 010123****}.
     */
    private static String maskNormalizedLastFourDigits(String normalized) {
        if (normalized.length() <= 4) {
            return "****";
        }
        return normalized.substring(0, normalized.length() - 4) + "****";
    }

    @Nested
    @DisplayName("마스킹 유틸")
    class 마스킹_유틸 {

        @Test
        @DisplayName("11자리 매치는 뒷 4자리를 *로 마스킹한다")
        void shouldMaskLastFourDigitsForElevenDigitMatch() {
            assertThat(maskLastFourDigits("01012345678")).isEqualTo("0101234****");
            assertThat(maskNormalizedLastFourDigits("01012345678")).isEqualTo("0101234****");
        }

        @Test
        @DisplayName("10자리 매치는 뒷 4자리를 *로 마스킹한다")
        void shouldMaskLastFourDigitsForTenDigitMatch() {
            assertThat(maskLastFourDigits("0101234567")).isEqualTo("010123****");
            assertThat(maskNormalizedLastFourDigits("0101234567")).isEqualTo("010123****");
        }

        @Test
        @DisplayName("하이픈 포함 매치의 하이픈은 보존한다")
        void shouldPreserveHyphenWhenMasking() {
            assertThat(maskLastFourDigits("010-1234-5678")).isEqualTo("010-1234-****");
            assertThat(maskLastFourDigits("010-123-4567")).isEqualTo("010-123-****");
        }

        @Test
        @DisplayName("9자리 매치는 뒷 4자리를 *로 마스킹한다")
        void shouldMaskLastFourDigitsForNineDigitMatch() {
            assertThat(maskLastFourDigits("021234567")).isEqualTo("02123****");
            assertThat(maskNormalizedLastFourDigits("021234567")).isEqualTo("02123****");
        }
    }

    @Nested
    @DisplayName("스캔 패턴 매칭")
    class 스캔_패턴_매칭 {

        @Test
        @DisplayName("서울 유선번호(02-1234-5678)는 패턴에 매칭된다")
        void shouldMatchSeoulLandline() {
            Matcher matcher = PHONE_PATTERN.matcher("연락처: 02-1234-5678 입니다");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("02-1234-5678");
        }

        @Test
        @DisplayName("서울 유선번호 9자리(02-123-4567)도 패턴에 매칭된다")
        void shouldMatchSeoulLandlineNineDigits() {
            Matcher matcher = PHONE_PATTERN.matcher("contact=02-123-4567;");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("02-123-4567");
        }

        @Test
        @DisplayName("지역 유선번호(031-555-0001)는 패턴에 매칭된다")
        void shouldMatchRegionalLandline() {
            Matcher matcher = PHONE_PATTERN.matcher("tel: 031-555-0001 / ext.");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("031-555-0001");
        }

        @Test
        @DisplayName("070 인터넷전화(070-1234-5678)는 패턴에 매칭된다")
        void shouldMatchVoip() {
            Matcher matcher = PHONE_PATTERN.matcher("voip 070-1234-5678 ok");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("070-1234-5678");
        }

        @Test
        @DisplayName("모바일(010-1234-5678)은 기존대로 패턴에 매칭된다")
        void shouldMatchMobile() {
            Matcher matcher = PHONE_PATTERN.matcher("mobile=010-1234-5678");
            assertThat(matcher.find()).isTrue();
            assertThat(matcher.group()).isEqualTo("010-1234-5678");
        }

        @Test
        @DisplayName("앞뒤 숫자 경계가 있으면 매칭하지 않는다 (타임스탬프 false positive 차단)")
        void shouldNotMatchWhenAdjacentToOtherDigits() {
            assertThat(PHONE_PATTERN.matcher("12021234567890").find()).isFalse();
            assertThat(PHONE_PATTERN.matcher("9070123456789").find()).isFalse();
        }

        @Test
        @DisplayName("허용되지 않는 prefix(070 외 07X 또는 02 외 0X)는 매칭하지 않는다")
        void shouldNotMatchUnsupportedPrefix() {
            assertThat(PHONE_PATTERN.matcher("0712345678").find()).isFalse();
            assertThat(PHONE_PATTERN.matcher("0712-345-6789").find()).isFalse();
            assertThat(PHONE_PATTERN.matcher("0801234567").find()).isFalse();
        }
    }

    @Nested
    @DisplayName("화이트리스트 위반 검출")
    class 화이트리스트_위반_검출 {

        @Test
        @DisplayName("화이트리스트 외 유선번호 fixture는 위반으로 검출된다")
        void shouldDetectNonWhitelistedLandline() {
            String fakeFixture = "String unknownPhone = \"02-9999-8888\";";
            List<String> violations = scanLineForViolations(fakeFixture);

            assertThat(violations).hasSize(1);
            assertThat(violations.get(0)).contains("02-9999-****");
            assertThat(violations.get(0)).contains("normalized=029999****");
        }

        @Test
        @DisplayName("화이트리스트에 등록된 유선번호 fixture는 위반으로 검출되지 않는다")
        void shouldNotDetectWhitelistedLandline() {
            String okFixture = "String dummyPhone = \"02-1234-5678\";";
            List<String> violations = scanLineForViolations(okFixture);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("화이트리스트 외 070 인터넷전화 fixture는 위반으로 검출된다")
        void shouldDetectNonWhitelistedVoip() {
            String fakeFixture = "String unknownVoip = \"070-9876-5432\";";
            List<String> violations = scanLineForViolations(fakeFixture);

            assertThat(violations).hasSize(1);
            assertThat(violations.get(0)).contains("070-9876-****");
        }

        private List<String> scanLineForViolations(String line) {
            List<String> violations = new ArrayList<>();
            Matcher matcher = PHONE_PATTERN.matcher(line);
            while (matcher.find()) {
                String matched = matcher.group();
                String normalized = matched.replace("-", "");
                if (normalized.length() < 9 || normalized.length() > 11) {
                    continue;
                }
                if (DUMMY_PHONE_WHITELIST.contains(normalized)) {
                    continue;
                }
                violations.add(
                        "test:1 -> " + maskLastFourDigits(matched)
                                + " (normalized=" + maskNormalizedLastFourDigits(normalized) + ")"
                );
            }
            return violations;
        }
    }
}
