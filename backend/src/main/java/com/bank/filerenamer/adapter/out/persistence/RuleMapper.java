package com.bank.filerenamer.adapter.out.persistence;

import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleEntity;
import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleVersionEntity;
import com.bank.filerenamer.domain.model.RenameRule;

/** Conversión entre la regla de dominio y sus representaciones JPA. */
final class RuleMapper {

    private RuleMapper() {
    }

    static RenameRule toDomain(RenameRuleEntity e) {
        return new RenameRule(e.getId(), e.getCode(), e.getDescription(), e.getPattern(),
                e.getTargetTemplate(), e.getSourceDateFormat(), e.getPriority(), e.isActive(), e.getVersion());
    }

    static RenameRule toDomain(RenameRuleVersionEntity e) {
        return new RenameRule(e.getRuleId(), e.getCode(), e.getDescription(), e.getPattern(),
                e.getTargetTemplate(), e.getSourceDateFormat(), e.getPriority(), e.isActive(), e.getVersion());
    }

    /** Copia el estado del dominio sobre la entidad (alta o modificación). */
    static void copyToEntity(RenameRule rule, RenameRuleEntity e) {
        e.setCode(rule.code());
        e.setDescription(rule.description());
        e.setPattern(rule.pattern());
        e.setTargetTemplate(rule.targetTemplate());
        e.setSourceDateFormat(rule.sourceDateFormat());
        e.setPriority(rule.priority());
        e.setActive(rule.active());
        e.setVersion(rule.version());
        e.setUpdatedAt(java.time.Instant.now());
    }
}
