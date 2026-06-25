package com.bank.filerenamer.integration;

import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.port.in.BucketUseCase;
import com.bank.filerenamer.domain.port.in.EnqueueProcessUseCase;
import com.bank.filerenamer.domain.port.in.QueryResultsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo asíncrono completo sobre infraestructura real (LocalStack S3 + SQS + PostgreSQL):
 * encola una solicitud y verifica que el worker, por su cuenta, la procesa y persiste la ejecución.
 */
class ProcessAsyncIT extends AbstractIntegrationTest {

    @Autowired
    BucketUseCase bucket;

    @Autowired
    EnqueueProcessUseCase enqueueProcess;

    @Autowired
    QueryResultsUseCase queryResults;

    @Test
    void enqueuedJobIsProcessedByWorker() throws InterruptedException {
        bucket.seedSampleFiles();
        int runsBefore = queryResults.listRuns().size();

        String jobId = enqueueProcess.enqueue();
        assertThat(jobId).isNotBlank();

        // El worker SQS debe recoger el mensaje y crear una nueva ejecución sin intervención.
        ProcessRun latest = waitForNewRun(runsBefore);

        assertThat(latest.total()).isEqualTo(8);
        assertThat(latest.transformed()).isGreaterThanOrEqualTo(6);
    }

    private ProcessRun waitForNewRun(int runsBefore) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < deadline) {
            List<ProcessRun> runs = queryResults.listRuns();
            if (runs.size() > runsBefore) {
                return mostRecent(runs);
            }
            Thread.sleep(500);
        }
        throw new AssertionError("El worker no procesó el trabajo encolado dentro del tiempo esperado");
    }

    private static ProcessRun mostRecent(List<ProcessRun> runs) {
        return runs.stream().max((a, b) -> {
            Instant ia = a.executedAt() == null ? Instant.MIN : a.executedAt();
            Instant ib = b.executedAt() == null ? Instant.MIN : b.executedAt();
            return ia.compareTo(ib);
        }).orElseThrow();
    }
}
