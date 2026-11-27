package com.personal.marketnote.common.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

// #4945/#4947 후속: 7개 서비스 어댑터에서 @Scheduled 메서드가 @SchedulerLock 어노테이션을 가지는지 검증.
// 화이트리스트로 인스턴스 로컬 SseEmitter ping 등 다중 인스턴스 중복 무관 잡을 제외한다 (#4911 정합).
// #4947 추가: @SchedulerLock의 lockAtMostFor 명시 강제 — 기본값(Long.MIN_VALUE) 사용 금지.
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

    public static ArchRule schedulerLockMethodsMustDeclareLockAtMostFor(Set<String> whitelist) {
        return methods()
            .that().areAnnotatedWith(SchedulerLock.class)
            .and(new NotInWhitelistPredicate(whitelist))
            .should(declareNonEmptyLockAtMostFor())
            .because("@SchedulerLock의 lockAtMostFor를 명시하지 않으면 EnableSchedulerLock의 defaultLockAtMostFor가 적용되어 "
                + "장기 잡(PT2H+)이 5분 후 다른 인스턴스에 락 탈취되어 중복 실행될 수 있다 (#4947)")
            .allowEmptyShould(true);
    }

    private static ArchCondition<JavaMethod> declareNonEmptyLockAtMostFor() {
        return new ArchCondition<>("@SchedulerLock(lockAtMostFor = ...) is explicitly declared (non-empty)") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                SchedulerLock annotation = method.reflect().getAnnotation(SchedulerLock.class);
                if (annotation == null) {
                    return;
                }
                String lockAtMostFor = annotation.lockAtMostFor();
                if (lockAtMostFor == null || lockAtMostFor.isEmpty()) {
                    events.add(SimpleConditionEvent.violated(
                        method,
                        "Method " + method.getFullName() + " 는 @SchedulerLock의 lockAtMostFor 명시가 필요하다 (#4947)"
                    ));
                }
            }
        };
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
