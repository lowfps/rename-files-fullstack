package com.bank.filerenamer.adapter.in.web.dto;

import com.bank.filerenamer.domain.model.ProcessRun;

import java.time.Instant;
import java.util.List;

/** Ejecución del proceso con el resumen del panel de control y el detalle por archivo. */
public record ProcessRunResponse(
        Long id,
        Instant executedAt,
        long rulesetVersion,
        Summary summary,
        List<RenameResultResponse> results
) {

    public record Summary(long total, long transformed, long errors, long noMapeado) {
    }

    public static ProcessRunResponse from(ProcessRun run) {
        Summary summary = new Summary(run.total(), run.transformed(), run.errors(), run.noMapeado());
        List<RenameResultResponse> results = run.results().stream()
                .map(RenameResultResponse::from).toList();
        return new ProcessRunResponse(run.id(), run.executedAt(), run.rulesetVersion(), summary, results);
    }
}
