package com.bank.filerenamer.domain.port.out;

import com.bank.filerenamer.domain.model.RenameRule;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia el catálogo de reglas y su historial de versiones.
 */
public interface RuleRepositoryPort {

    List<RenameRule> findAll();

    /** Reglas activas; el motor las ordena por prioridad. */
    List<RenameRule> findActive();

    Optional<RenameRule> findById(Long id);

    /**
     * Persiste la regla (alta o modificación), guarda un snapshot inmutable en el historial
     * e incrementa la versión global del catálogo.
     */
    RenameRule save(RenameRule rule);

    /** Historial de versiones de una regla, de más reciente a más antigua. */
    List<RenameRule> findVersions(Long ruleId);

    /** Versión global actual del catálogo (cambia ante cualquier alta/modificación). */
    long currentRulesetVersion();
}
