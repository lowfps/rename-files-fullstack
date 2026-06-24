package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.exception.InvalidDateException;
import com.bank.filerenamer.domain.model.DateFormat;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Normaliza la fecha embebida en el nombre de origen al formato canónico {@code yyyyMMdd}
 * que espera el banco, interpretando el token según el {@link DateFormat} declarado por la regla.
 */
public class DateNormalizer {

    /**
     * @param rawDate token de fecha capturado (ej. {@code 20260430} o {@code 20263004})
     * @param format  formato de entrada declarado por la regla
     * @return fecha en formato canónico {@code yyyyMMdd}
     * @throws InvalidDateException si el token no es una fecha válida para el formato
     */
    public String normalize(String rawDate, DateFormat format) {
        if (!format.hasDate()) {
            throw new IllegalArgumentException("No se puede normalizar una fecha con formato NONE");
        }
        if (rawDate == null || rawDate.isBlank()) {
            throw new InvalidDateException("Token de fecha ausente para formato " + format);
        }
        try {
            LocalDate date = LocalDate.parse(rawDate, format.parser());
            return date.format(DateFormat.CANONICAL);
        } catch (DateTimeParseException ex) {
            throw new InvalidDateException(
                    "Fecha inválida '" + rawDate + "' para formato " + format);
        }
    }
}
