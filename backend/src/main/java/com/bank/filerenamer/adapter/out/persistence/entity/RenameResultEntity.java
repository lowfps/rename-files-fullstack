package com.bank.filerenamer.adapter.out.persistence.entity;

import com.bank.filerenamer.domain.model.RenameStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rename_result")
public class RenameResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "run_id")
    private ProcessRunEntity run;

    private String sourceFileName;
    private String targetFileName;

    @Enumerated(EnumType.STRING)
    private RenameStatus status;

    private String appliedRuleCode;
    private Integer appliedRuleVersion;
    private String message;

    protected RenameResultEntity() {
    }

    public RenameResultEntity(String sourceFileName, String targetFileName, RenameStatus status,
                              String appliedRuleCode, Integer appliedRuleVersion, String message) {
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
        this.status = status;
        this.appliedRuleCode = appliedRuleCode;
        this.appliedRuleVersion = appliedRuleVersion;
        this.message = message;
    }

    public void setRun(ProcessRunEntity run) {
        this.run = run;
    }

    public Long getId() {
        return id;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public RenameStatus getStatus() {
        return status;
    }

    public String getAppliedRuleCode() {
        return appliedRuleCode;
    }

    public Integer getAppliedRuleVersion() {
        return appliedRuleVersion;
    }

    public String getMessage() {
        return message;
    }
}
