package com.kb.healthcare.common.business.record.domain.entity;

import com.kb.healthcare.common.business.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "record")
@Entity
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Comment("걸음수")
    @Column(name = "steps", nullable = false)
    private Integer steps;

    @Comment("부터(기간)")
    @Column(name = "period_from", nullable = false)
    private LocalDateTime periodFrom;

    @Comment("까지(기간)")
    @Column(name = "period_to", nullable = false)
    private LocalDateTime periodTo;

    @Comment("거리")
    @Column(name = "distance_value", nullable = false)
    private Float distanceValue;

    @Comment("거리단위")
    @Column(name = "distance_unit", nullable = false, columnDefinition = "CHAR(2)")
    private String distanceUnit;

    @Comment("칼로리")
    @Column(name = "calories_value", nullable = false)
    private Float caloriesValue;

    @Comment("칼로리 단위")
    @Column(name = "calories_unit", nullable = false, columnDefinition = "CHAR(4)")
    private String caloriesUnit;

    @Comment("등록일시")
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDatetime;


}
