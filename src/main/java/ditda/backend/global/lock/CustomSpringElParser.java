package ditda.backend.global.lock;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomSpringElParser {

	private static final ExpressionParser PARSER = new SpelExpressionParser();

	public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {

		StandardEvaluationContext context = new StandardEvaluationContext();

		for (int i = 0; i < parameterNames.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}

		return PARSER.parseExpression(key).getValue(context, Object.class);
	}
}
