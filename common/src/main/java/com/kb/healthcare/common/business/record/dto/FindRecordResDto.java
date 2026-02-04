package com.kb.healthcare.common.business.record.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDateTime;

public record FindRecordResDto(
    LocalDateTime localDateTime,
    String memo,
    String type,
    String name,
    String vender,
    String productName,
    int mode,
    String recordKey,
    int steps,
    LocalDateTime periodFrom,
    LocalDateTime periodTo,
    Float distanceValue,
    String distanceUnit,
    Float caloriesValue,
    String caloriesUnit
) {
    @Builder
    @QueryProjection
    public FindRecordResDto {

    }
}
