package com.personal.marketnote.common.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchRule;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

// #4945 후속: 7개 서비스 어댑터에서 @Scheduled 메서드가 @SchedulerLock 어노테이션을 가지는지 검증.
// 화이트리스트로 인스턴스 로컬 SseEmitter ping 등 다중 인스턴스 중복 무관 잡을 제외한다 (#4911 정합).
public final class SchedulerLockArchRules {

    private SchedulerLockArchRules() {
    }

    public static ArchRule scheduledMethodsShouldHaveSchedulerLock(Set<String> whitelist) {
        return methods()
                .that().areAnnotatedWith(Scheduled.class)
                .and(new NotInWhitelistPredicate(whitelist))
                .should().beAnnotatedWith(SchedulerLock.class)
                .because("@Scheduled 메서드는 다중 인스턴스 환경에서 중복 실행을 막기 위해 @SchedulerLock을 가져야 한다 (#4911)")
                .allowEmptyShould(true);
    }

    private static final class NotInWhitelistPredicate extends DescribedPredicate<JavaMethod> {
        private final Set<String> whitelist;

        private NotInWhitelistPredicate(Set<String> whitelist) {
            super("not in #4911 SchedulerLock whitelist");
            this.whitelist = whitelist;
        }

        @Override
        public boolean test(JavaMethod method) {
            // JavaMethod.getFullName()은 파라미터 시그니처까지 포함한 풀네임 → 오버로드 메서드 구분 가능.
            return !whitelist.contains(method.getFullName());
        }
    }
}
