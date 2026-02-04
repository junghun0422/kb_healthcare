package com.kb.healthcare.record.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.utils.MultiDateFormatDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "건강 기록 Request")
public record RecordRequestDto(
    @Schema(description = "기록 키")
    String recordkey,
    @Schema(description = "기록 데이터")
    Data data,
    @JsonDeserialize(using = MultiDateFormatDeserializer.class)
    @Schema(description = "마지막 업데이트 일시")
    LocalDateTime lastUpdate,
    @Schema(description = "유형")
    String type
) {

    @Schema(description = "기록 데이터")
    public record Data(
        @Schema(description = "메모")
        String memo,
        @Schema(description = "기록")
        List<Entry> entries,
        @Schema(description = "출처")
        Source source
    ) {

    }

    public record Entry(
        @Schema(description = "기간")
        Period period,
        @Schema(description = "거리")
        Distance distance,
        @Schema(description = "칼로리")
        Calorie calories,
        @Schema(description = "걸음수")
        float steps
    ) {

    }

    @Schema(description = "기간")
    public record Period(
        @Schema(description = "~부터", example = "2024-11-15 00:00:00")
        @JsonDeserialize(using = MultiDateFormatDeserializer.class)
        LocalDateTime from,
        @JsonDeserialize(using = MultiDateFormatDeserializer.class)
        @Schema(description = "~까지", example = "2024-11-15 00:10:00")
        LocalDateTime to
    ) {

    }

    @Schema(description = "이동거리")
    public record Distance(
        @Schema(description = "단위", example = "km")
        String unit,
        @Schema(description = "거리 수치", example = "0.04223")
        float value
    ) {

    }

    @Schema(description = "칼로리")
    public record Calorie(
        @Schema(description = "단위", example = "kcal")
        String unit,
        @Schema(description = "소모한 칼로리", example = "2.03")
        float value
    ) {

    }

    public record Source(
        @Schema(description = "유형", example = "9")
        int mode,
        @Schema(description = "장비")
        Product product,
        @Schema(description = "중계 앱", example = "SamsungHealth")
        String name,
        @Schema(description = "")
        String type
    ) {

    }

    @Schema(description = "장비")
    public record Product(
        @Schema(description = "OS", example = "Android")
        String name,
        @Schema(description = "브랜드", example = "Samsung")
        String vender
    ) {

    }

    @Builder
    public Record builder(User user, RecordRequestDto.Entry entry, com.kb.healthcare.common.business.record.domain.entity.Source source) {
        Period period = entry.period;
        Distance distance = entry.distance;
        Calorie calories = entry.calories;

        return Record.builder()
                        .source(source)
                        .steps(Integer.parseInt(String.valueOf(entry.steps)))
                        .periodFrom(period.from)
                        .periodTo(period.to)
                        .distanceUnit(distance.unit)
                        .distanceValue(distance.value)
                        .caloriesUnit(calories.unit)
                        .caloriesValue(calories.value)
                    .build();
    }

}