package com.bank.filerenamer.adapter.out.persistence.entity;

import com.bank.filerenamer.domain.model.DateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Snapshot inmutable de una regla en una versión concreta. */
@Entity
@Table(name = "rename_rule_version")
public class RenameRuleVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    private String code;
    private String description;
    private String pattern;
    private String targetTemplate;

    @Enumerated(EnumType.STRING)
    private DateFormat sourceDateFormat;

    private int priority;
    private boolean active;
    private int version;

    @Column(name = "created_at")
    private Instant createdAt;

    protected RenameRuleVersionEntity() {
    }

    public static RenameRuleVersionEntity from(RenameRuleEntity rule) {
        RenameRuleVersionEntity v = new RenameRuleVersionEntity();
        v.ruleId = rule.getId();
        v.code = rule.getCode();
        v.description = rule.getDescription();
        v.pattern = rule.getPattern();
        v.targetTemplate = rule.getTargetTemplate();
        v.sourceDateFormat = rule.getSourceDateFormat();
        v.priority = rule.getPriority();
        v.active = rule.isActive();
        v.version = rule.getVersion();
        v.createdAt = Instant.now();
        return v;
    }

    public Long getId() {
        return id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getPattern() {
        return pattern;
    }

    public String getTargetTemplate() {
        return targetTemplate;
    }

    public DateFormat getSourceDateFormat() {
        return sourceDateFormat;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
