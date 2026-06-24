package com.bank.filerenamer.adapter.out.persistence.jpa;

import com.bank.filerenamer.adapter.out.persistence.entity.RulesetVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RulesetVersionJpaRepository extends JpaRepository<RulesetVersionEntity, Integer> {
}
