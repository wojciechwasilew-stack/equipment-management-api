package com.tequipy.equipment.infrastructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.tequipy.equipment";
    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + ".application..";
    private static final String ADAPTER_PACKAGE = BASE_PACKAGE + ".adapter..";
    private static final String CONFIG_PACKAGE = BASE_PACKAGE + ".config..";

    private static final String ADAPTER_IN_REST_PACKAGE = BASE_PACKAGE + ".adapter.in.rest..";
    private static final String ADAPTER_IN_MESSAGING_PACKAGE = BASE_PACKAGE + ".adapter.in.messaging..";
    private static final String ADAPTER_OUT_PERSISTENCE_PACKAGE = BASE_PACKAGE + ".adapter.out.persistence..";
    private static final String ADAPTER_OUT_MESSAGING_PACKAGE = BASE_PACKAGE + ".adapter.out.messaging..";

    private static final String PORT_IN_PACKAGE = BASE_PACKAGE + ".application.port.in..";
    private static final String PORT_OUT_PACKAGE = BASE_PACKAGE + ".application.port.out..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        void domain_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax.persistence..",
                            "javax.transaction.."
                    );

            rule.check(importedClasses);
        }

        @Test
        void domain_should_not_depend_on_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_PACKAGE, CONFIG_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void domain_should_not_depend_on_application() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void domain_should_not_depend_on_infrastructure_libraries() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.hibernate..",
                            "com.fasterxml..",
                            "org.mapstruct..",
                            "io.swagger.."
                    );

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerRules {

        @Test
        void application_should_not_depend_on_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_PACKAGE, CONFIG_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void application_should_not_depend_on_spring() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax.persistence.."
                    );

            rule.check(importedClasses);
        }

        @Test
        void application_services_should_only_depend_on_ports_and_domain() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".application.service..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            BASE_PACKAGE + ".application..",
                            BASE_PACKAGE + ".domain..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    );

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Ports Rules")
    class PortsRules {

        @Test
        void inbound_ports_should_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(PORT_IN_PACKAGE)
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces();

            rule.check(importedClasses);
        }

        @Test
        void outbound_ports_should_be_interfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(PORT_OUT_PACKAGE)
                    .and().haveSimpleNameEndingWith("Port")
                    .should().beInterfaces();

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Adapter Independence Rules")
    class AdapterIndependenceRules {

        @Test
        void rest_adapter_should_not_depend_on_persistence_adapter() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_IN_REST_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_OUT_PERSISTENCE_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void rest_adapter_should_not_depend_on_messaging_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_IN_REST_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_IN_MESSAGING_PACKAGE, ADAPTER_OUT_MESSAGING_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void persistence_adapter_should_not_depend_on_rest_adapter() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_OUT_PERSISTENCE_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_IN_REST_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void persistence_adapter_should_not_depend_on_messaging_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_OUT_PERSISTENCE_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_IN_MESSAGING_PACKAGE, ADAPTER_OUT_MESSAGING_PACKAGE);

            rule.check(importedClasses);
        }

        @Test
        void inbound_messaging_adapter_should_not_depend_on_other_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_IN_MESSAGING_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            ADAPTER_IN_REST_PACKAGE,
                            ADAPTER_OUT_PERSISTENCE_PACKAGE,
                            ADAPTER_OUT_MESSAGING_PACKAGE
                    );

            rule.check(importedClasses);
        }

        @Test
        void outbound_messaging_adapter_should_not_depend_on_other_adapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_OUT_MESSAGING_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            ADAPTER_IN_REST_PACKAGE,
                            ADAPTER_IN_MESSAGING_PACKAGE,
                            ADAPTER_OUT_PERSISTENCE_PACKAGE
                    );

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Dependency Direction Rules")
    class DependencyDirectionRules {

        @Test
        void adapters_should_depend_on_application_ports_not_services() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(BASE_PACKAGE + ".application.service..");

            rule.check(importedClasses);
        }

        @Test
        void config_should_not_depend_on_domain() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(CONFIG_PACKAGE)
                    .should().dependOnClassesThat()
                    .resideInAPackage(DOMAIN_PACKAGE);

            rule.check(importedClasses);
        }
    }
}
