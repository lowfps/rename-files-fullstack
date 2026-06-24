package com.bank.filerenamer.integration;

import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.model.RenameStatus;
import com.bank.filerenamer.domain.port.in.BucketUseCase;
import com.bank.filerenamer.domain.port.in.ProcessFilesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo completo sobre infraestructura real (LocalStack S3 + PostgreSQL + Flyway):
 * siembra el bucket, ejecuta el motor con el catálogo semilla y verifica resultados y persistencia.
 */
class ProcessFlowIT extends AbstractIntegrationTest {

    @Autowired
    BucketUseCase bucket;

    @Autowired
    ProcessFilesUseCase processFiles;

    @Test
    void seedsBucketAndProcessesFilesWithSeededRules() {
        bucket.seedSampleFiles();

        ProcessRun run = processFiles.process();

        assertThat(run.id()).isNotNull();
        assertThat(run.total()).isEqualTo(8);
        assertThat(run.transformed()).isGreaterThanOrEqualTo(6);
        assertThat(run.noMapeado()).isGreaterThanOrEqualTo(1);

        // El CDT desmaterializado se normaliza con su fecha.
        assertThat(targetOf(run, "PHO_CD_DES_20260430"))
                .isEqualTo("01_Estructura CDT Desmaterializado_20260430");
        // Archivo no estructurado queda sin mapear.
        assertThat(statusOf(run, "PrendasPajaro.txt")).isEqualTo(RenameStatus.NO_MAPEADO);
    }

    private static String targetOf(ProcessRun run, String source) {
        return result(run, source).targetFileName();
    }

    private static RenameStatus statusOf(ProcessRun run, String source) {
        return result(run, source).status();
    }

    private static RenameResult result(ProcessRun run, String source) {
        return run.results().stream()
                .filter(r -> r.sourceFileName().equals(source))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró resultado para " + source));
    }
}
