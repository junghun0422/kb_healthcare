package com.kb.healthcare.common.business.record.domain.entity;

import com.kb.healthcare.common.business.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(name = "steps", nullable = false)
    private Integer steps;

    @Column(name = "period_from", nullable = false)
    private LocalDateTime periodFrom;

    @Column(name = "period_to", nullable = false)
    private LocalDateTime periodTo;

    @Column(name = "period_date", insertable = false, updatable = false)
    private LocalDate periodDate;

    @Column(name = "distance_value", nullable = false)
    private Float distanceValue;

    @Column(name = "distance_unit", nullable = false, columnDefinition = "CHAR(2)")
    private String distanceUnit;

    @Column(name = "calories_value", nullable = false)
    private Float caloriesValue;

    @Column(name = "calories_unit", nullable = false, columnDefinition = "CHAR(4)")
    private String caloriesUnit;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDatetime;

}
