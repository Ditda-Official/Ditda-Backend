package ditda.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ditda.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class CodeConventionTest {

	@ArchTest
	static final ArchRule 엔티티에_Setter_를_두지_않는다 =
		noMethods().that().areDeclaredInClassesThat().resideInAPackage("..entity..")
			.should().haveNameMatching("set[A-Z].*");

	@ArchTest
	static final ArchRule 컨트롤러에_트랜잭션을_두지_않는다 =
		noMethods().that().areDeclaredInClassesThat().resideInAPackage("..controller..")
			.should().beAnnotatedWith(Transactional.class);

	@ArchTest
	static final ArchRule 리포지토리는_인터페이스여야_한다 =
		classes().that().resideInAPackage("..repository")
			.and().areTopLevelClasses()
			.should().beInterfaces();

	@ArchTest
	static final ArchRule 필드주입을_사용하지_않는다 =
		noFields().should().beAnnotatedWith(Autowired.class);
}
