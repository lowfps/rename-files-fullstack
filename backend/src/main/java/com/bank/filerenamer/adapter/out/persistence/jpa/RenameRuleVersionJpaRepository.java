package com.bank.filerenamer.adapter.out.persistence.jpa;

import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenameRuleVersionJpaRepository extends JpaRepository<RenameRuleVersionEntity, Long> {

    List<RenameRuleVersionEntity> findByRuleIdOrderByVersionDesc(Long ruleId);
}
