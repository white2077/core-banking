package com.core.bank.demo.entity;

import java.sql.Timestamp;

import jakarta.persistence.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "send_approval_at")
    private Timestamp sendApprovalAt;

    @Column(name = "send_approval_by")
    private String sendApprovalBy;

    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "status")
    private String status;

    @PrePersist
    protected void onCreate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            this.createdBy = authentication.getName();
        }

        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
