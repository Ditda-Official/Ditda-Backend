package ditda.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "ditda.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class NamingConventionTest {

	@ArchTest
	static final ArchRule 컨트롤러_네이밍 =
		classes().that().resideInAPackage("..controller..")
			.and().areTopLevelClasses()
			.should().haveSimpleNameEndingWith("Controller")
			.andShould().beAnnotatedWith(RestController.class);

	@ArchTest
	static final ArchRule 서비스_네이밍 =
		classes().that().resideInAPackage("..service..")
			.and().areTopLevelClasses()
			.should().haveSimpleNameEndingWith("Service")
			.andShould().beAnnotatedWith(Service.class);

	@ArchTest
	static final ArchRule 파사드_네이밍 =
		classes().that().resideInAPackage("..facade..")
			.and().areTopLevelClasses()
			.should().haveSimpleNameEndingWith("Facade");

	@ArchTest
	static final ArchRule 리포지토리_네이밍 =
		classes().that().resideInAPackage("..repository")
			.and().areTopLevelClasses()
			.should().haveSimpleNameEndingWith("Repository");
}
