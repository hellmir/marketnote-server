package com.personal.marketnote.reward.archunit;

import com.personal.marketnote.common.archunit.SchedulerLockArchRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Set;

@AnalyzeClasses(
        packages = "com.personal.marketnote.reward",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
class SchedulerLockArchUnitTest {

    @ArchTest
    static final ArchRule scheduledMethodsShouldHaveSchedulerLock =
            SchedulerLockArchRules.scheduledMethodsShouldHaveSchedulerLock(Set.of());
    @ArchTest
    static final ArchRule schedulerLockMethodsMustDeclareLockAtMostFor =
            SchedulerLockArchRules.schedulerLockMethodsMustDeclareLockAtMostFor(Set.of());
}
