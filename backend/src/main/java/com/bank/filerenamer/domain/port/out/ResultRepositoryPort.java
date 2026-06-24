package com.bank.filerenamer.domain.port.out;

import com.bank.filerenamer.domain.model.ProcessRun;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar las ejecuciones del proceso y sus resultados.
 */
public interface ResultRepositoryPort {

    ProcessRun save(ProcessRun run);

    Optional<ProcessRun> findById(Long id);

    /** Ejecuciones, de más reciente a más antigua. */
    List<ProcessRun> findAll();
}
