package com.personal.marketnote.common.archunit;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public final class DomainExceptionArchRules {

    // #4710 기준 화이트리스트 — 도메인 not-found 가 아닌 시스템 설정 에러.
    // rename(KafkaSslTruststoreMissingException 등)은 본 이슈 범위 밖.
    private static final Set<String> NOT_FOUND_WHITELIST = Set.of(
            "com.personal.marketnote.common.configuration.kafka.exception.KafkaSslTruststoreNotFoundException"
    );

    private DomainExceptionArchRules() {
    }

    public static ArchRule alreadyExistsExceptionsMustExtendDomainAlreadyExistsException() {
        return classes()
                .that().haveSimpleNameEndingWith("AlreadyExistsException")
                .and().doNotHaveFullyQualifiedName(DomainAlreadyExistsException.class.getName())
                .should().beAssignableTo(DomainAlreadyExistsException.class)
                .because("'*AlreadyExistsException' 네이밍은 도메인 중복 발생을 표현하므로 "
                        + "DomainAlreadyExistsException 을 상속해야 한다 (#4710)")
                .allowEmptyShould(true);
    }

    public static ArchRule notFoundExceptionsMustExtendDomainNotFoundException() {
        return classes()
                .that().haveSimpleNameEndingWith("NotFoundException")
                .and().doNotHaveFullyQualifiedName(DomainNotFoundException.class.getName())
                .and(new NotInWhitelistPredicate())
                .should().beAssignableTo(DomainNotFoundException.class)
                .because("'*NotFoundException' 네이밍은 도메인 조회 실패를 표현하므로 "
                        + "DomainNotFoundException 을 상속해야 한다 (#4710)")
                .allowEmptyShould(true);
    }

    public static ArchRule domainExceptionConstructorsMustNotUseStringFormat() {
        return noClasses()
                .that().areAssignableTo(DomainNotFoundException.class)
                .or().areAssignableTo(DomainAlreadyExistsException.class)
                .should(callStringFormatInConstructor())
                .because("도메인 예외 생성자 내부에서 String.format 으로 식별자/PII 메시지를 조립하면 "
                        + "응답 body 및 로그에 식별자가 유출될 위험이 있다 (#4710, #4694)")
                .allowEmptyShould(true);
    }

    private static ArchCondition<JavaClass> callStringFormatInConstructor() {
        return new ArchCondition<>("not call String.format inside any constructor") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getConstructors().forEach(constructor ->
                        constructor.getMethodCallsFromSelf().forEach(call ->
                                evaluateCall(javaClass, call, events)
                        )
                );
            }

            private void evaluateCall(JavaClass javaClass, JavaMethodCall call, ConditionEvents events) {
                if (!isStringFormatCall(call)) {
                    return;
                }
                String message = String.format(
                        "Class %s 의 생성자에서 String.format 을 호출한다 (%s)",
                        javaClass.getName(),
                        call.getSourceCodeLocation()
                );
                events.add(SimpleConditionEvent.violated(call, message));
            }

            private boolean isStringFormatCall(JavaMethodCall call) {
                JavaMethod target = call.getTarget().resolveMember().orElse(null);
                if (target == null) {
                    return false;
                }
                boolean isStringOwner = target.getOwner().isEquivalentTo(String.class);
                boolean isFormatName = "format".equals(target.getName());
                return isStringOwner && isFormatName;
            }
        };
    }

    private static final class NotInWhitelistPredicate extends DescribedPredicate<JavaClass> {
        private NotInWhitelistPredicate() {
            super("not in #4710 whitelist");
        }

        @Override
        public boolean test(JavaClass javaClass) {
            return !NOT_FOUND_WHITELIST.contains(javaClass.getFullName());
        }
    }
}
