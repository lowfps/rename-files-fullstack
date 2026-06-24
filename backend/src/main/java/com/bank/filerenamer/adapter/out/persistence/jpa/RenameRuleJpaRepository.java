package com.bank.filerenamer.adapter.out.persistence.jpa;

import com.bank.filerenamer.adapter.out.persistence.entity.RenameRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenameRuleJpaRepository extends JpaRepository<RenameRuleEntity, Long> {

    List<RenameRuleEntity> findByActiveTrueOrderByPriorityAsc();

    List<RenameRuleEntity> findAllByOrderByPriorityAsc();
}
