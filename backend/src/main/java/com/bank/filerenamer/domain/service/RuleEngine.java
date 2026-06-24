package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.exception.InvalidDateException;
import com.bank.filerenamer.domain.exception.TemplateRenderException;
import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.model.RenameRule;
import com.bank.filerenamer.domain.model.S3File;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Núcleo del reto: aplica el catálogo de reglas a un archivo de origen.
 *
 * <p>La lógica (este motor) está separada de la configuración (las {@link RenameRule}). Las reglas
 * se evalúan por prioridad y la primera que casa gana; los grupos nombrados de la regex se inyectan
 * en la plantilla destino y la fecha se normaliza al formato canónico del banco. Escalar el sistema
 * consiste en añadir reglas, nunca en modificar este motor.</p>
 */
public class RuleEngine {

    /** Nombre del grupo de captura reservado para la fecha embebida. */
    private static final String DATE_GROUP = "date";

    private static final Pattern NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private final DateNormalizer dateNormalizer;
    private final TemplateRenderer templateRenderer;

    public RuleEngine(DateNormalizer dateNormalizer, TemplateRenderer templateRenderer) {
        this.dateNormalizer = dateNormalizer;
        this.templateRenderer = templateRenderer;
    }

    /**
     * Aplica las reglas a un archivo. Solo participan las reglas activas, evaluadas por prioridad
     * ascendente (menor número = mayor prioridad).
     */
    public RenameResult apply(S3File file, List<RenameRule> rules) {
        List<RenameRule> candidates = rules.stream()
                .filter(RenameRule::active)
                .sorted(Comparator.comparingInt(RenameRule::priority))
                .toList();

        String source = file.key();
        String name = file.nameWithoutExtension();

        for (RenameRule rule : candidates) {
            Matcher matcher = rule.compiledPattern().matcher(name);
            if (!matcher.matches()) {
                continue;
            }
            try {
                Map<String, String> values = extractValues(rule, matcher);
                String target = templateRenderer.render(rule.targetTemplate(), values);
                return RenameResult.transformed(source, target, rule);
            } catch (InvalidDateException | TemplateRenderException ex) {
                return RenameResult.error(source, rule, ex.getMessage());
            }
        }
        return RenameResult.noMapeado(source);
    }

    private Map<String, String> extractValues(RenameRule rule, Matcher matcher) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String group : namedGroups(rule.pattern())) {
            String raw = matcher.group(group);
            if (raw != null) {
                values.put(group, raw);
            }
        }
        if (rule.sourceDateFormat().hasDate()) {
            String raw = values.get(DATE_GROUP);
            values.put(DATE_GROUP, dateNormalizer.normalize(raw, rule.sourceDateFormat()));
        }
        return values;
    }

    /** Extrae los nombres de los grupos de captura nombrados declarados en la regex. */
    static List<String> namedGroups(String pattern) {
        List<String> names = new ArrayList<>();
        Matcher m = NAMED_GROUP.matcher(pattern);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }
}
