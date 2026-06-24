package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.exception.RunNotFoundException;
import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.port.in.QueryResultsUseCase;
import com.bank.filerenamer.domain.port.out.ResultRepositoryPort;

import java.util.List;

/**
 * Consulta de ejecuciones para el panel de control.
 */
public class QueryResultsService implements QueryResultsUseCase {

    private final ResultRepositoryPort resultRepository;

    public QueryResultsService(ResultRepositoryPort resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public List<ProcessRun> listRuns() {
        return resultRepository.findAll();
    }

    @Override
    public ProcessRun getRun(Long id) {
        return resultRepository.findById(id).orElseThrow(() -> new RunNotFoundException(id));
    }
}
