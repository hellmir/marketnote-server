package com.personal.marketnote.common.scheduler;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class SchedulerLockMetadataTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.personal.marketnote.common");
    }

    @Test
    @DisplayName("common 모듈의 모든 @Scheduled 메서드는 @SchedulerLock을 가진다")
    void allScheduledMethodsInCommonShouldHaveSchedulerLock() {
        methods()
                .that().areAnnotatedWith(Scheduled.class)
                .should().beAnnotatedWith(SchedulerLock.class)
                .check(importedClasses);
    }
}
