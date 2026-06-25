package com.bank.filerenamer.adapter.in.web;

import com.bank.filerenamer.adapter.in.web.dto.EnqueueResponse;
import com.bank.filerenamer.adapter.in.web.dto.ProcessRunResponse;
import com.bank.filerenamer.adapter.in.web.dto.RenameResultResponse;
import com.bank.filerenamer.domain.port.in.EnqueueProcessUseCase;
import com.bank.filerenamer.domain.port.in.ProcessFilesUseCase;
import com.bank.filerenamer.domain.port.in.QueryResultsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ejecución del proceso (síncrona y asíncrona) y consultas del panel de control. */
@RestController
public class ProcessController {

    private final ProcessFilesUseCase processFiles;
    private final EnqueueProcessUseCase enqueueProcess;
    private final QueryResultsUseCase queryResults;

    public ProcessController(ProcessFilesUseCase processFiles,
                             EnqueueProcessUseCase enqueueProcess,
                             QueryResultsUseCase queryResults) {
        this.processFiles = processFiles;
        this.enqueueProcess = enqueueProcess;
        this.queryResults = queryResults;
    }

    @PostMapping("/api/process")
    public ProcessRunResponse process() {
        return ProcessRunResponse.from(processFiles.process());
    }

    /** Encola el procesamiento (SQS) y responde 202 de inmediato; el worker lo ejecuta aparte. */
    @PostMapping("/api/process/async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EnqueueResponse processAsync() {
        return EnqueueResponse.queued(enqueueProcess.enqueue());
    }

    @PostMapping("/api/process/{runId}/reprocess")
    public ProcessRunResponse reprocess(@PathVariable Long runId) {
        return ProcessRunResponse.from(processFiles.reprocess(runId));
    }

    @GetMapping("/api/runs")
    public List<ProcessRunResponse> listRuns() {
        return queryResults.listRuns().stream().map(ProcessRunResponse::from).toList();
    }

    @GetMapping("/api/runs/{id}")
    public ProcessRunResponse getRun(@PathVariable Long id) {
        return ProcessRunResponse.from(queryResults.getRun(id));
    }

    @GetMapping("/api/results")
    public List<RenameResultResponse> results(@RequestParam Long runId) {
        return queryResults.getRun(runId).results().stream().map(RenameResultResponse::from).toList();
    }
}
