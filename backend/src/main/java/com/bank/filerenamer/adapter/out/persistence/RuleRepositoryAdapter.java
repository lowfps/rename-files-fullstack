package com.bank.filerenamer.adapter.out.persistence;

import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleEntity;
import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleVersionEntity;
import com.bank.filerenamer.adapter.out.persistence.entity.RulesetVersionEntity;
import com.bank.filerenamer.adapter.out.persistence.jpa.RenameRuleJpaRepository;
import com.bank.filerenamer.adapter.out.persistence.jpa.RenameRuleVersionJpaRepository;
import com.bank.filerenamer.adapter.out.persistence.jpa.RulesetVersionJpaRepository;
import com.bank.filerenamer.domain.model.RenameRule;
import com.bank.filerenamer.domain.port.out.RuleRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia del catálogo de reglas. Cada {@link #save} actualiza el estado vigente,
 * guarda un snapshot inmutable en el historial e incrementa la versión global del catálogo, de forma
 * atómica.
 */
@Repository
public class RuleRepositoryAdapter implements RuleRepositoryPort {

    private final RenameRuleJpaRepository ruleRepository;
    private final RenameRuleVersionJpaRepository versionRepository;
    private final RulesetVersionJpaRepository rulesetRepository;

    public RuleRepositoryAdapter(RenameRuleJpaRepository ruleRepository,
                                 RenameRuleVersionJpaRepository versionRepository,
                                 RulesetVersionJpaRepository rulesetRepository) {
        this.ruleRepository = ruleRepository;
        this.versionRepository = versionRepository;
        this.rulesetRepository = rulesetRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenameRule> findAll() {
        return ruleRepository.findAllByOrderByPriorityAsc().stream().map(RuleMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenameRule> findActive() {
        return ruleRepository.findByActiveTrueOrderByPriorityAsc().stream().map(RuleMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RenameRule> findById(Long id) {
        return ruleRepository.findById(id).map(RuleMapper::toDomain);
    }

    @Override
    @Transactional
    public RenameRule save(RenameRule rule) {
        RenameRuleEntity entity = rule.id() == null
                ? new RenameRuleEntity()
                : ruleRepository.findById(rule.id()).orElseGet(RenameRuleEntity::new);
        RuleMapper.copyToEntity(rule, entity);
        RenameRuleEntity saved = ruleRepository.save(entity);

        versionRepository.save(RenameRuleVersionEntity.from(saved));

        RulesetVersionEntity ruleset = rulesetRepository
                .findById(RulesetVersionEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("ruleset_version no inicializado"));
        ruleset.increment();
        rulesetRepository.save(ruleset);

        return RuleMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenameRule> findVersions(Long ruleId) {
        return versionRepository.findByRuleIdOrderByVersionDesc(ruleId).stream()
                .map(RuleMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long currentRulesetVersion() {
        return rulesetRepository.findById(RulesetVersionEntity.SINGLETON_ID)
                .map(RulesetVersionEntity::getVersion)
                .orElse(1L);
    }
}
