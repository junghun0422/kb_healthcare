package com.kb.healthcare.common.business.record.domain.entity;

import com.kb.healthcare.common.business.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "source")
@Entity
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @Column(name = "mode", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int mode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "vender", nullable = false)
    private String vender;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = true)
    private String type;

    @Column(name = "memo", nullable = true)
    private String memo;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @CreatedDate
    @Column(name = "create_datetime", nullable = false, updatable = false)
    private LocalDateTime createDatetime;

    @Builder
    public Source(
            String recordKey, int mode, String vender,
            String productName, String type, String name,
            String memo, LocalDateTime lastUpdate) {
        this.recordKey = recordKey;
        this.mode = mode;
        this.vender = vender;
        this.productName = productName;
        this.type = type;
        this.name = name;
        this.memo = memo;
        this.lastUpdate = lastUpdate;
    }


}