package com.personal.marketnote.common.scheduler;

import com.personal.marketnote.common.archunit.SchedulerLockArchRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Set;

// common 모듈의 모든 @Scheduled 메서드는 @SchedulerLock을 가진다 (#4911 정합).
// testFixtures의 SchedulerLockArchRules factory를 사용하여 7개 서비스와 동일 룰 적용 (#4945 사각지대 제거).
@AnalyzeClasses(
        packages = "com.personal.marketnote.common",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
class SchedulerLockMetadataTest {

    @ArchTest
    static final ArchRule scheduledMethodsShouldHaveSchedulerLock =
            SchedulerLockArchRules.scheduledMethodsShouldHaveSchedulerLock(Set.of());

    @ArchTest
    static final ArchRule schedulerLockMethodsMustDeclareLockAtMostFor =
            SchedulerLockArchRules.schedulerLockMethodsMustDeclareLockAtMostFor(Set.of());
}
