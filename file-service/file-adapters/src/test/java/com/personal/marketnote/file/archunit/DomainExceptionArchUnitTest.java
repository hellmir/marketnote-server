package com.personal.marketnote.file.archunit;

import com.personal.marketnote.common.archunit.DomainExceptionArchRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.personal.marketnote.file",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
class DomainExceptionArchUnitTest {

    @ArchTest
    static final ArchRule alreadyExistsMustExtendDomainAlreadyExists =
            DomainExceptionArchRules.alreadyExistsExceptionsMustExtendDomainAlreadyExistsException();

    @ArchTest
    static final ArchRule notFoundMustExtendDomainNotFound =
            DomainExceptionArchRules.notFoundExceptionsMustExtendDomainNotFoundException();

    @ArchTest
    static final ArchRule domainExceptionConstructorsMustNotUseStringFormat =
            DomainExceptionArchRules.domainExceptionConstructorsMustNotUseStringFormat();
}
