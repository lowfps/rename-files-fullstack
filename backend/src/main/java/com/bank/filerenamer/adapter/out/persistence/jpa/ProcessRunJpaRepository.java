package com.bank.filerenamer.adapter.out.persistence.jpa;

import com.bank.filerenamer.adapter.out.persistence.entity.ProcessRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessRunJpaRepository extends JpaRepository<ProcessRunEntity, Long> {

    List<ProcessRunEntity> findAllByOrderByExecutedAtDesc();
}
