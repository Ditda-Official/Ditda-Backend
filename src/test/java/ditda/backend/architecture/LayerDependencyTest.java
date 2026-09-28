package ditda.backend.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ditda.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class LayerDependencyTest {

	@ArchTest
	static final ArchRule 컨트롤러는_리포지토리를_직접_의존하지_않는다 =
		noClasses().that().resideInAPackage("..controller..")
			.should().dependOnClassesThat().resideInAPackage("..repository..");

	@ArchTest
	static final ArchRule 컨트롤러는_아무도_의존하지_않는다 =
		noClasses().that().resideOutsideOfPackage("..controller..")
			.should().dependOnClassesThat().resideInAPackage("..controller..");

	@ArchTest
	static final ArchRule 엔티티는_상위_계층을_의존하지_않는다 =
		noClasses().that().resideInAPackage("..entity..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..service..", "..controller..", "..facade..");

	@ArchTest
	static final ArchRule 컨트롤러는_엔티티를_반환하지_않는다 =
		noMethods().that().areDeclaredInClassesThat().resideInAPackage("..controller..")
			.should().haveRawReturnType(resideInAPackage("..entity.."));
}
