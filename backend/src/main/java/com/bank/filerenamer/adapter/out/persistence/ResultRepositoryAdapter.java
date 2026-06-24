package com.bank.filerenamer.adapter.out.persistence;

import com.bank.filerenamer.adapter.out.persistence.entity.ProcessRunEntity;
import com.bank.filerenamer.adapter.out.persistence.entity.RenameResultEntity;
import com.bank.filerenamer.adapter.out.persistence.jpa.ProcessRunJpaRepository;
import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.port.out.ResultRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia de las ejecuciones del proceso y sus resultados por archivo. */
@Repository
public class ResultRepositoryAdapter implements ResultRepositoryPort {

    private final ProcessRunJpaRepository runRepository;

    public ResultRepositoryAdapter(ProcessRunJpaRepository runRepository) {
        this.runRepository = runRepository;
    }

    @Override
    @Transactional
    public ProcessRun save(ProcessRun run) {
        ProcessRunEntity entity = new ProcessRunEntity(run.executedAt(), run.rulesetVersion());
        for (RenameResult r : run.results()) {
            entity.addResult(new RenameResultEntity(r.sourceFileName(), r.targetFileName(),
                    r.status(), r.appliedRuleCode(), r.appliedRuleVersion(), r.message()));
        }
        return toDomain(runRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessRun> findById(Long id) {
        return runRepository.findById(id).map(ResultRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessRun> findAll() {
        return runRepository.findAllByOrderByExecutedAtDesc().stream()
                .map(ResultRepositoryAdapter::toDomain).toList();
    }

    private static ProcessRun toDomain(ProcessRunEntity e) {
        List<RenameResult> results = e.getResults().stream()
                .map(r -> new RenameResult(r.getSourceFileName(), r.getTargetFileName(), r.getStatus(),
                        r.getAppliedRuleCode(), r.getAppliedRuleVersion(), r.getMessage()))
                .toList();
        return new ProcessRun(e.getId(), e.getExecutedAt(), e.getRulesetVersion(), results);
    }
}
