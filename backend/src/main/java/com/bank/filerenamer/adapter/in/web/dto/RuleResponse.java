package com.bank.filerenamer.adapter.in.web.dto;

import com.bank.filerenamer.domain.model.DateFormat;
import com.bank.filerenamer.domain.model.RenameRule;

public record RuleResponse(
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
    public static RuleResponse from(RenameRule r) {
        return new RuleResponse(r.id(), r.code(), r.description(), r.pattern(), r.targetTemplate(),
                r.sourceDateFormat(), r.priority(), r.active(), r.version());
    }
}
