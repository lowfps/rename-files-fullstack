package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.exception.RunNotFoundException;
import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.model.RenameRule;
import com.bank.filerenamer.domain.model.S3File;
import com.bank.filerenamer.domain.port.in.ProcessFilesUseCase;
import com.bank.filerenamer.domain.port.out.FileStoragePort;
import com.bank.filerenamer.domain.port.out.ResultRepositoryPort;
import com.bank.filerenamer.domain.port.out.RuleRepositoryPort;
import com.bank.filerenamer.domain.service.RuleEngine;

import java.util.List;

/**
 * Orquesta el caso de uso de procesamiento: lee el bucket, aplica el motor de reglas con el
 * catálogo vigente y persiste la ejecución. No contiene lógica de transformación (delegada al
 * {@link RuleEngine}) ni dependencias de framework.
 */
public class ProcessFilesService implements ProcessFilesUseCase {

    private final FileStoragePort fileStorage;
    private final RuleRepositoryPort ruleRepository;
    private final ResultRepositoryPort resultRepository;
    private final RuleEngine ruleEngine;

    public ProcessFilesService(FileStoragePort fileStorage,
                               RuleRepositoryPort ruleRepository,
                               ResultRepositoryPort resultRepository,
                               RuleEngine ruleEngine) {
        this.fileStorage = fileStorage;
        this.ruleRepository = ruleRepository;
        this.resultRepository = resultRepository;
        this.ruleEngine = ruleEngine;
    }

    @Override
    public ProcessRun process() {
        List<S3File> files = fileStorage.listFiles();
        List<RenameRule> rules = ruleRepository.findActive();
        long rulesetVersion = ruleRepository.currentRulesetVersion();

        List<RenameResult> results = files.stream()
                .map(file -> ruleEngine.apply(file, rules))
                .toList();

        return resultRepository.save(ProcessRun.of(rulesetVersion, results));
    }

    @Override
    public ProcessRun reprocess(Long sourceRunId) {
        resultRepository.findById(sourceRunId)
                .orElseThrow(() -> new RunNotFoundException(sourceRunId));
        // Reprocesa el bucket con las reglas vigentes (la versión del catálogo puede haber cambiado).
        return process();
    }
}
