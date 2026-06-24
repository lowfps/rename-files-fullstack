package com.bank.filerenamer.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Ejecución del proceso de renombramiento sobre el conjunto de archivos del bucket.
 * Agrupa los resultados por archivo y expone los totales del panel de control.
 *
 * @param id             identificador (nulo antes de persistir)
 * @param executedAt     instante de ejecución
 * @param rulesetVersion versión global del catálogo de reglas usada (trazabilidad de reprocesos)
 * @param results        detalle por archivo
 */
public record ProcessRun(
        Long id,
        Instant executedAt,
        long rulesetVersion,
        List<RenameResult> results
) {

    public ProcessRun {
        results = results == null ? List.of() : List.copyOf(results);
    }

    public static ProcessRun of(long rulesetVersion, List<RenameResult> results) {
        return new ProcessRun(null, Instant.now(), rulesetVersion, results);
    }

    public long total() {
        return results.size();
    }

    public long countByStatus(RenameStatus status) {
        return results.stream().filter(r -> r.status() == status).count();
    }

    public long transformed() {
        return countByStatus(RenameStatus.TRANSFORMED);
    }

    public long errors() {
        return countByStatus(RenameStatus.ERROR);
    }

    public long noMapeado() {
        return countByStatus(RenameStatus.NO_MAPEADO);
    }
}
