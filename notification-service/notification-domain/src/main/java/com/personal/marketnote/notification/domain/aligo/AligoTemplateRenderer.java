package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AligoTemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("#\\{([^}]+)}");

    private AligoTemplateRenderer() {
    }

    public static String render(String template, Map<String, String> variables) {
        if (FormatValidator.hasNoValue(template)) {
            return null;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variables.get(variableName);
            if (FormatValidator.hasNoValue(value)) {
                throw new InvalidAligoTemplateException("미해결 변수가 존재합니다: #{" + variableName + "}");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
