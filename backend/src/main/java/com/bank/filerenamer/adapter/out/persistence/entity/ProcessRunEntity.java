package com.bank.filerenamer.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_run")
public class ProcessRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant executedAt;
    private long rulesetVersion;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RenameResultEntity> results = new ArrayList<>();

    protected ProcessRunEntity() {
    }

    public ProcessRunEntity(Instant executedAt, long rulesetVersion) {
        this.executedAt = executedAt;
        this.rulesetVersion = rulesetVersion;
    }

    public void addResult(RenameResultEntity result) {
        result.setRun(this);
        this.results.add(result);
    }

    public Long getId() {
        return id;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public long getRulesetVersion() {
        return rulesetVersion;
    }

    public List<RenameResultEntity> getResults() {
        return results;
    }
}
