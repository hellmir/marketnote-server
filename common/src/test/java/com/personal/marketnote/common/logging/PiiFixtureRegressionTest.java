package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 fixture 회귀 방지 테스트.
 *
 * <p>배경: {@code PiiMaskingConverter}는 test profile에서 {@code %msg} 평문 출력을 허용한다.
 * CI 아티팩트에 테스트 로그가 적재될 때 실제 사용자 PII가 fixture에 포함되어 있으면 유출 경로가 된다.
 *
 * <p>규칙: 프로젝트 전체 {@code *Test.java}(빌드 산출물/로컬 히스토리 제외) 파일에 국내/글로벌 주요 실 메일 도메인이
 * 하드코딩되어 있는지 스캔한다. 발견 시 이 테스트가 실패하여 실PII 유입을 즉시 차단한다.
 *
 * <p>재현 쉘 명령:
 * <pre>{@code
 * grep -rn --include="*Test.java" -iE \
 *     '@(gmail|naver|daum|kakao|hanmail|nate|outlook|hotmail|yahoo|icloud|live)\.(com|net)' . \
 *     | grep -v '/build/' | grep -v '/.history/'
 * }</pre>
 */
class PiiFixtureRegressionTest {

    private static final List<String> FORBIDDEN_EMAIL_DOMAINS = List.of(
            "gmail.com",
            "naver.com",
            "daum.net",
            "kakao.com",
            "hanmail.net",
            "nate.com",
            "outlook.com",
            "hotmail.com",
            "yahoo.com",
            "icloud.com",
            "live.com"
    );

    private static final Pattern FORBIDDEN_EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@(gmail\\.com|naver\\.com|daum\\.net|kakao\\.com|hanmail\\.net|nate\\.com"
                    + "|outlook\\.com|hotmail\\.com|yahoo\\.com|icloud\\.com|live\\.com)",
            Pattern.CASE_INSENSITIVE
    );

    private static final String SELF_RELATIVE_PATH =
            "common/src/test/java/com/personal/marketnote/common/logging/PiiFixtureRegressionTest.java";

    private static final List<String> EXCLUDED_PATH_SEGMENTS = List.of(
            "/build/",
            "/.history/",
            "/.git/",
            "/.gradle/",
            "/bin/"
    );

    @Test
    @DisplayName("프로젝트 전체 테스트 파일에 실 PII 이메일 도메인이 하드코딩되어 있으면 실패한다")
    void shouldNotContainRealPiiEmailDomainInTestSources() throws IOException {
        Path projectRoot = resolveProjectRoot();
        Path selfAbsolute = projectRoot.resolve(SELF_RELATIVE_PATH).toAbsolutePath().normalize();
        List<String> violations = new ArrayList<>();
        long scannedCount;

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            scannedCount = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("Test.java"))
                    .filter(path -> !isExcluded(path))
                    .filter(path -> !isSelf(path, selfAbsolute))
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
                        "테스트 fixture에 실 PII 이메일 도메인이 하드코딩되어 있습니다. "
                                + "금지 도메인: %s. CI 로그 아티팩트 유출 방지를 위해 "
                                + "test.com / example.com 등 테스트 도메인으로 교체하십시오. 위반 목록:%n%s",
                        FORBIDDEN_EMAIL_DOMAINS,
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

    private boolean isSelf(Path path, Path selfAbsolute) {
        return path.toAbsolutePath().normalize().equals(selfAbsolute);
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
            Matcher matcher = FORBIDDEN_EMAIL_PATTERN.matcher(line);
            while (matcher.find()) {
                violations.add(relative + ":" + (index + 1) + " -> " + matcher.group());
            }
        }
    }
}
