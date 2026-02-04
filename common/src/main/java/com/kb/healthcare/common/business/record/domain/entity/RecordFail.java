package com.kb.healthcare.common.business.record.domain.entity;


import com.kb.healthcare.common.business.base.domain.entity.BaseDateTimeEntity;
import com.kb.healthcare.common.business.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "record_fail")
@Entity
public class RecordFail extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Comment("사용자 구분 키")
    @Column(name = "record_key", nullable = false)
    private String recordKey;

    @Comment("출처 시퀀스")
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Comment("건강 기록 실패 데이터")
    @Column(name = "entry_json", nullable = false, columnDefinition = "JSON")
    private String entryJson;


    @Comment("재시도 회수")
    @Column(name = "retry_count", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private int retryCount;

    @Comment("상태")
    @ColumnDefault("'PENDING'")
    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Builder
    public RecordFail (
            User user, String recordKey, Long sourceId,
            String entryJson, String status) {
        this.user = user;
        this.recordKey = recordKey;
        this.sourceId = sourceId;
        this.entryJson = entryJson;
        this.status = status;
    }

}
