package com.bank.filerenamer.domain.port.in;

import com.bank.filerenamer.domain.model.RenameRule;

import java.util.List;

/**
 * Puerto de entrada para administrar el catálogo de reglas (CRUD + versionamiento).
 */
public interface ManageRulesUseCase {

    List<RenameRule> listRules();

    RenameRule getRule(Long id);

    RenameRule createRule(RuleCommand command);

    RenameRule updateRule(Long id, RuleCommand command);

    /** Desactiva la regla (borrado lógico) generando una nueva versión. */
    RenameRule deactivateRule(Long id);

    List<RenameRule> getRuleVersions(Long id);
}
