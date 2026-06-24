package com.bank.filerenamer.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Contador global del catálogo de reglas (única fila, id = 1). */
@Entity
@Table(name = "ruleset_version")
public class RulesetVersionEntity {

    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    private long version;

    protected RulesetVersionEntity() {
    }

    public Integer getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void increment() {
        this.version++;
    }
}
