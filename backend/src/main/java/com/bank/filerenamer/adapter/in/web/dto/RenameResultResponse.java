package com.bank.filerenamer.adapter.in.web.dto;

import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.model.RenameStatus;

public record RenameResultResponse(
        String sourceFileName,
        String targetFileName,
        RenameStatus status,
        String appliedRuleCode,
        Integer appliedRuleVersion,
        String message
) {
    public static RenameResultResponse from(RenameResult r) {
        return new RenameResultResponse(r.sourceFileName(), r.targetFileName(), r.status(),
                r.appliedRuleCode(), r.appliedRuleVersion(), r.message());
    }
}
