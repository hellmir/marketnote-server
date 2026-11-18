package com.personal.marketnote.notification.archunit;

import com.personal.marketnote.common.archunit.SchedulerLockArchRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Set;

// SseHeartbeatScheduler.sendHeartbeat은 인스턴스 로컬 SseEmitter ping이라 다중 인스턴스 중복 무관 → 화이트리스트 (#4911 정합).
@AnalyzeClasses(
        packages = "com.personal.marketnote.notification",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
class SchedulerLockArchUnitTest {

    // JavaMethod.getFullName() 형식: "fqcn.method(paramTypes)". 오버로드 메서드 구분 가능.
    private static final Set<String> WHITELIST = Set.of(
            "com.personal.marketnote.notification.adapter.in.web.sse.scheduler.SseHeartbeatScheduler.sendHeartbeat()"
    );

    @ArchTest
    static final ArchRule scheduledMethodsShouldHaveSchedulerLock =
            SchedulerLockArchRules.scheduledMethodsShouldHaveSchedulerLock(WHITELIST);

    @ArchTest
    static final ArchRule schedulerLockMethodsMustDeclareLockAtMostFor =
            SchedulerLockArchRules.schedulerLockMethodsMustDeclareLockAtMostFor(Set.of());
}
