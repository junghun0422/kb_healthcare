package com.kb.healthcare.common.business.record.domain.entity;

import com.kb.healthcare.common.business.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Comment("사용자 구분 키")
    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @Comment("유형")
    @Column(name = "mode", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int mode;

    @Comment("OS")
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Comment("브랜드")
    @Column(name = "vender", nullable = false)
    private String vender;

    @Comment("요청출처")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("유형")
    @Column(name = "type", nullable = true)
    private String type;

    @Comment("메모")
    @Column(name = "memo", nullable = true)
    private String memo;

    @Comment("마지막 수정일시")
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @Comment("등록일시")
    @CreatedDate
    @Column(name = "create_datetime", nullable = false, updatable = false)
    private LocalDateTime createDatetime;

    @Builder
    public Source(
            User user, String recordKey, int mode
            , String vender, String productName, String type
            , String name, String memo, LocalDateTime lastUpdate) {
        this.user = user;
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