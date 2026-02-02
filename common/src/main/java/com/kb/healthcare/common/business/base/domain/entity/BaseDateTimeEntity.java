package com.kb.healthcare.common.business.base.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseDateTimeEntity {

    @CreatedDate
    @Column(name = "create_datetime", nullable = false, updatable = false)
    private LocalDateTime createDatetime;

    @LastModifiedDate
    @Column(name = "update_datetime", nullable = true, updatable = true)
    private LocalDateTime updateDatetime;

    @PrePersist
    public void prePersist() { this.updateDatetime = null; }

    public void actionUpdateDateTime() { this.updateDatetime = LocalDateTime.now(); }

}
