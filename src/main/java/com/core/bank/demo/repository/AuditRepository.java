package com.core.bank.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.core.bank.demo.entity.AuditRecord;

public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}
