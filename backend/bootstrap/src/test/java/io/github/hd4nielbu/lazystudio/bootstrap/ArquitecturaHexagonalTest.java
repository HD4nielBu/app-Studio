package io.github.hd4nielbu.lazystudio.bootstrap;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Hace cumplir la regla de dependencias del README: siempre hacia adentro. */
@AnalyzeClasses(packages = "io.github.hd4nielbu.lazystudio", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    private static final String BASE = "io.github.hd4nielbu.lazystudio";

    @ArchTest
    static final ArchRule capas_hacia_adentro = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Dominio").definedBy(BASE + ".domain..")
            .layer("Aplicacion").definedBy(BASE + ".application..")
            .layer("Adaptadores").definedBy(BASE + ".adapters..")
            .layer("Arranque").definedBy(BASE + ".bootstrap..")
            .whereLayer("Arranque").mayNotBeAccessedByAnyLayer()
            .whereLayer("Adaptadores").mayOnlyBeAccessedByLayers("Arranque")
            .whereLayer("Aplicacion").mayOnlyBeAccessedByLayers("Adaptadores", "Arranque")
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Aplicacion", "Adaptadores", "Arranque");

    @ArchTest
    static final ArchRule dominio_solo_java = noClasses()
            .that().resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideOutsideOfPackages(BASE + ".domain..", "java..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacion_sin_frameworks = noClasses()
            .that().resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideOutsideOfPackages(BASE + ".domain..", BASE + ".application..", "java..")
            .allowEmptyShould(true);

    // adapters.in.rest, adapters.out.persistencia, adapters.out.ia… no se conocen entre sí.
    @ArchTest
    static final ArchRule adaptadores_independientes = slices()
            .matching(BASE + ".adapters.(*).(*)..")
            .should().notDependOnEachOther()
            .allowEmptyShould(true);
}
