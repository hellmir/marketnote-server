package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{(\\w+)}");

    private TemplateRenderer() {
    }

    public static String render(String template, Map<String, String> variables) {
        if (FormatValidator.hasNoValue(template)) {
            return null;
        }

        validateAllTemplateKeysResolved(template, variables);

        String rendered = template;
        if (FormatValidator.hasValue(variables)) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return rendered;
    }

    private static void validateAllTemplateKeysResolved(String template, Map<String, String> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (FormatValidator.hasNoValue(variables) || !variables.containsKey(key)) {
                throw new InvalidTemplateVariableException(matcher.group(0));
            }
        }
    }
}
