package com.core.bank.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "audit_record")
@Getter
@Setter
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "action")
    private String action;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "old_value", columnDefinition = "json")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "json")
    private String newValue;

    @Column(name = "actor")
    private String actor;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
