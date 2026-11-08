package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.DisplayName;
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
 * <p>규칙: 프로젝트 전체 {@code *Test.java}(빌드 산출물/로컬 히스토리 제외) 파일에서 {@code 010}으로
 * 시작하는 한국 휴대전화 패턴을 추출하여 하이픈 제거 후 11자리로 정규화한다. 정규화된 값이
 * {@link #DUMMY_PHONE_WHITELIST}에 포함되지 않으면 회귀 실패로 처리하여 실PII 유입을 차단한다.
 *
 * <p>실성명: 한국어 2~3글자 성명 패턴은 Java import/클래스명/변수명과 false positive 중첩이 크기
 * 때문에 현 시점에서는 허용 성명 {@link #ALLOWED_DUMMY_NAMES} 상수만 정의하여 fixture 작성 가이드로
 * 남긴다. 적극 스캔은 후속 이슈에서 재검토한다.
 *
 * <p>재현 쉘 명령:
 * <pre>{@code
 * grep -rhoE --include="*Test.java" '(^|[^0-9])010-?[0-9]{3,4}-?[0-9]{4}([^0-9]|$)' . \
 *     | grep -v '/build/' | grep -v '/.history/' \
 *     | grep -oE '010-?[0-9]{3,4}-?[0-9]{4}' | tr -d '-' \
 *     | awk 'length($0)==11' | sort -u
 * }</pre>
 */
class PiiFixturePhoneRegressionTest {

    /**
     * 테스트 공용 더미 전화번호(정규화된 11자리). 현재 fixture 초기 감사 결과 기반으로 구성.
     *
     * <p>추가 시 원칙:
     * <ul>
     *   <li>반복 자릿수(예: {@code 01011111111}, {@code 01000000000})</li>
     *   <li>순차/공용 시퀀스(예: {@code 01012345678}, {@code 01098765432})</li>
     *   <li>테스트 데이터 시드로 명백한 패턴(예: {@code 010xxxx0001})</li>
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
            "01099998888"
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

    /** 앞뒤 숫자 경계를 확인하여 타임스탬프/시퀀스 리터럴 오탐을 차단한다. */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)010-?\\d{3,4}-?\\d{4}(?!\\d)"
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
                if (normalized.length() != 11) {
                    continue;
                }
                if (DUMMY_PHONE_WHITELIST.contains(normalized)) {
                    continue;
                }
                violations.add(relative + ":" + (index + 1) + " -> " + matched + " (normalized=" + normalized + ")");
            }
        }
    }
}
