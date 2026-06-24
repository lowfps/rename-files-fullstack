package com.bank.filerenamer.domain.model;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regla de renombramiento: es 100% configuración (persistida, editable y versionada),
 * separada de la lógica del motor. Añadir o cambiar una regla NO requiere tocar código.
 *
 * @param id               identificador (nulo para reglas nuevas)
 * @param code             código de layout destino del banco (ej. {@code 01}, {@code 13}, {@code 37})
 * @param description      nombre legible de la estructura destino
 * @param pattern          regex con grupos nombrados sobre el nombre SIN extensión
 *                         (ej. {@code ^PHO_CD_DES_(?<date>\d{8})$})
 * @param targetTemplate   plantilla del nombre estándar con placeholders {@code {grupo}}
 *                         (ej. {@code 01_Estructura CDT Desmaterializado_{date}})
 * @param sourceDateFormat formato con el que interpretar el grupo {@code date}
 * @param priority         menor = mayor prioridad; resuelve múltiples patrones similares
 * @param active           reglas inactivas no participan en el procesamiento
 * @param version          versión de la regla (incrementa en cada modificación)
 */
public record RenameRule(
        Long id,
        String code,
        String description,
        String pattern,
        String targetTemplate,
        DateFormat sourceDateFormat,
        int priority,
        boolean active,
        int version
) {

    public RenameRule {
        require(code, "code");
        require(description, "description");
        require(pattern, "pattern");
        require(targetTemplate, "targetTemplate");
        if (sourceDateFormat == null) {
            throw new IllegalArgumentException("sourceDateFormat es obligatorio");
        }
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("Regex inválida en la regla '" + code + "': " + ex.getMessage());
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio");
        }
    }

    /** Regex compilada lista para evaluar el motor. */
    public Pattern compiledPattern() {
        return Pattern.compile(pattern);
    }
}
