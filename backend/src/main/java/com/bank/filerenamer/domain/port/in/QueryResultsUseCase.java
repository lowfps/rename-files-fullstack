package com.bank.filerenamer.domain.port.in;

import com.bank.filerenamer.domain.model.ProcessRun;

import java.util.List;

/**
 * Puerto de entrada para consultar ejecuciones y alimentar el panel de control.
 */
public interface QueryResultsUseCase {

    List<ProcessRun> listRuns();

    ProcessRun getRun(Long id);
}
