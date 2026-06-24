package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.exception.TemplateRenderException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renderiza la plantilla destino sustituyendo placeholders {@code {nombre}} por los valores
 * extraídos del archivo de origen (grupos nombrados de la regex, con la fecha ya normalizada).
 */
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_]*)\\}");

    /**
     * @param template plantilla con placeholders (ej. {@code 01_Estructura CDT Desmaterializado_{date}})
     * @param values   valores disponibles por nombre de placeholder
     * @return nombre estandarizado
     * @throws TemplateRenderException si la plantilla referencia un placeholder sin valor
     */
    public String render(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = values.get(key);
            if (value == null) {
                throw new TemplateRenderException(
                        "La plantilla referencia '{" + key + "}' pero no hay valor disponible");
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
