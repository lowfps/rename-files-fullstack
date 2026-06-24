package com.bank.filerenamer.domain.model;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Formato de la fecha embebida en el nombre del archivo de origen.
 *
 * <p>El motor de reglas usa este valor para interpretar el grupo de captura {@code date}
 * y normalizarlo siempre al formato canónico {@code yyyyMMdd} esperado por el banco.</p>
 */
public enum DateFormat {

    /** Año-Mes-Día: {@code AAAAMMDD} (ej. 20260430). */
    YYYYMMDD(DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT)),

    /** Año-Día-Mes: {@code AAAADDMM} (ej. 20263004). */
    YYYYDDMM(DateTimeFormatter.ofPattern("uuuuddMM").withResolverStyle(ResolverStyle.STRICT)),

    /** El nombre no contiene fecha embebida. */
    NONE(null);

    /** Formato canónico de salida que espera el banco. */
    public static final DateTimeFormatter CANONICAL = DateTimeFormatter.ofPattern("uuuuMMdd");

    private final DateTimeFormatter parser;

    DateFormat(DateTimeFormatter parser) {
        this.parser = parser;
    }

    /** Formato de entrada con el que se parsea el token de fecha. Nulo para {@link #NONE}. */
    public DateTimeFormatter parser() {
        return parser;
    }

    public boolean hasDate() {
        return this != NONE;
    }
}
