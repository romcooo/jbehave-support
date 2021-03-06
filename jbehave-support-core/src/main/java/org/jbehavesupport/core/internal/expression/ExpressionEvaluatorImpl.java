package org.jbehavesupport.core.internal.expression;

import static org.jbehavesupport.core.support.TestContextUtil.unescape;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;
import org.jbehavesupport.core.expression.ExpressionCommand;
import org.jbehavesupport.core.expression.ExpressionEvaluator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jbehavesupport.core.support.TestContextUtil;

@Slf4j
public class ExpressionEvaluatorImpl implements ExpressionEvaluator {
    //
    private static final Pattern EXPRESSION = Pattern.compile("(.*)(?<!\\\\)\\{(([^\\{\\}]|\\\\\\{|\\\\\\})*)(?<!\\\\)\\}(.*)", Pattern.DOTALL);

    private final Map<String, String> aliases;
    private final Map<String, String> shorthands;

    private Map<String, ExpressionCommand> commands;

    public ExpressionEvaluatorImpl(Map<String, ExpressionCommand> commands) {
        this.commands = commands;
        aliases = new HashMap<>();
        shorthands = new HashMap<>();
        shorthands.put("CP", "TEST_CONTEXT_COPY");
        shorthands.put("EMPTY", "EMPTY_STRING");
        shorthands.put("DP", "DATE_PARSE");
        shorthands.put("FD", "FORMAT_DATE");
        shorthands.put("UC", "UPPER_CASE");
        shorthands.put("LC", "LOWER_CASE");
    }

    /**
     * Evaluate string.
     *
     * @param expression the value
     * @return the string
     */
    @Override
    public Object evaluate(final String expression) {
        Object returnValue = expression;
        Matcher m = EXPRESSION.matcher(returnValue.toString());
        while (m.find()) {
            String subExpression = m.group(2);
            Object valObj = evaluateExpression(subExpression);
            if (valObj != null) {
                if (!m.group(1).isEmpty() || !m.group(4).isEmpty()) {
                    returnValue = m.group(1) + valObj.toString() + m.group(4);
                } else {
                    returnValue = valObj;
                }
            } else {
                returnValue = "";
            }
            m = EXPRESSION.matcher(returnValue.toString());
        }

        if (returnValue instanceof String) {
            returnValue = unescape(returnValue);
        }

        return returnValue;
    }

    private Object evaluateExpression(String expression) {
        if (aliases.containsKey(expression)) {
            log.debug("For expression: {} using alias: {}", expression, aliases.get(expression));
            expression = aliases.get(expression);
        }

        String commandName = CommandHelper.commandName(expression);
        String[] commandParams = CommandHelper.commandParams(expression);
        ExpressionCommand evaluationCommand = commands.get(resolveBeanName(commandName));

        if (evaluationCommand == null) {
            throw new IllegalStateException("Unable to evaluate: '" + expression + "'. Check command: " + commandName);
        }

        String[] unescapedParams = Arrays.stream(commandParams)
            .map(TestContextUtil::unescape)
            .toArray(String[]::new);
        return TestContextUtil.escape(evaluationCommand.execute(unescapedParams));
    }

    private String resolveBeanName(String commandName) {
        if (shorthands.containsKey(commandName)) {
            return resolveBeanName(shorthands.get(commandName));
        }
        return convertToLowerCamelCase(commandName + "_COMMAND");
    }

    private String convertToLowerCamelCase(String upperUnderscoreString) {
        return StringUtils.uncapitalize(StringUtils.remove(WordUtils.capitalizeFully(upperUnderscoreString, '_'), '_'));
    }

    @Override
    public void registerAlias(String alias, String command) {
        this.aliases.put(alias, command);
    }

    @Override
    public void registerShorthand(String shortHand, String command) {
        this.shorthands.putIfAbsent(shortHand, command);
    }
}
