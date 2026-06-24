package com.bank.filerenamer.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifica la regla de dependencia hexagonal: el dominio no conoce frameworks ni adaptadores,
 * y la aplicación no conoce los adaptadores ni Spring/AWS.
 */
@AnalyzeClasses(packages = "com.bank.filerenamer")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainHasNoFrameworkDependencies = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "software.amazon..",
                    "jakarta.persistence..",
                    "com.bank.filerenamer.application..",
                    "com.bank.filerenamer.adapter..",
                    "com.bank.filerenamer.config..");

    @ArchTest
    static final ArchRule applicationDependsOnlyOnDomain = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "software.amazon..",
                    "jakarta.persistence..",
                    "com.bank.filerenamer.adapter..",
                    "com.bank.filerenamer.config..");

    @ArchTest
    static final ArchRule webAdapterDoesNotDependOnPersistence = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..");
}
