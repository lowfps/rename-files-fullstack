package com.bank.filerenamer.domain.port.in;

import com.bank.filerenamer.domain.model.DateFormat;

/**
 * Datos de entrada para crear o actualizar una regla (sin id ni versión: los gestiona el dominio).
 */
public record RuleCommand(
        String code,
        String description,
        String pattern,
        String targetTemplate,
        DateFormat sourceDateFormat,
        int priority,
        boolean active
) {
}
