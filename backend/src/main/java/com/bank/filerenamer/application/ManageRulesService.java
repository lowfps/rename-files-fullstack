package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.exception.RuleNotFoundException;
import com.bank.filerenamer.domain.model.RenameRule;
import com.bank.filerenamer.domain.port.in.ManageRulesUseCase;
import com.bank.filerenamer.domain.port.in.RuleCommand;
import com.bank.filerenamer.domain.port.out.RuleRepositoryPort;

import java.util.List;

/**
 * Administra el catálogo de reglas. La versión de cada regla se incrementa en cada cambio;
 * el adaptador de persistencia guarda el snapshot histórico y sube la versión global del catálogo.
 */
public class ManageRulesService implements ManageRulesUseCase {

    private final RuleRepositoryPort ruleRepository;

    public ManageRulesService(RuleRepositoryPort ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public List<RenameRule> listRules() {
        return ruleRepository.findAll();
    }

    @Override
    public RenameRule getRule(Long id) {
        return ruleRepository.findById(id).orElseThrow(() -> new RuleNotFoundException(id));
    }

    @Override
    public RenameRule createRule(RuleCommand command) {
        RenameRule rule = new RenameRule(
                null,
                command.code(),
                command.description(),
                command.pattern(),
                command.targetTemplate(),
                command.sourceDateFormat(),
                command.priority(),
                command.active(),
                1);
        return ruleRepository.save(rule);
    }

    @Override
    public RenameRule updateRule(Long id, RuleCommand command) {
        RenameRule existing = getRule(id);
        RenameRule updated = new RenameRule(
                existing.id(),
                command.code(),
                command.description(),
                command.pattern(),
                command.targetTemplate(),
                command.sourceDateFormat(),
                command.priority(),
                command.active(),
                existing.version() + 1);
        return ruleRepository.save(updated);
    }

    @Override
    public RenameRule deactivateRule(Long id) {
        RenameRule existing = getRule(id);
        RenameRule deactivated = new RenameRule(
                existing.id(),
                existing.code(),
                existing.description(),
                existing.pattern(),
                existing.targetTemplate(),
                existing.sourceDateFormat(),
                existing.priority(),
                false,
                existing.version() + 1);
        return ruleRepository.save(deactivated);
    }

    @Override
    public List<RenameRule> getRuleVersions(Long id) {
        getRule(id);
        return ruleRepository.findVersions(id);
    }
}
