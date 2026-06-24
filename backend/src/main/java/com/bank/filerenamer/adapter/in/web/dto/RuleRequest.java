package com.bank.filerenamer.adapter.in.web.dto;

import com.bank.filerenamer.domain.model.DateFormat;
import com.bank.filerenamer.domain.port.in.RuleCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de alta/actualización de una regla. */
public record RuleRequest(
        @NotBlank String code,
        @NotBlank String description,
        @NotBlank String pattern,
        @NotBlank String targetTemplate,
        @NotNull DateFormat sourceDateFormat,
        int priority,
        boolean active
) {
    public RuleCommand toCommand() {
        return new RuleCommand(code, description, pattern, targetTemplate, sourceDateFormat, priority, active);
    }
}
