package com.kb.healthcare.common.business.record.repository;


import com.kb.healthcare.common.business.record.dto.FindRecordResDto;
import com.kb.healthcare.common.business.record.dto.QFindRecordResDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

import static com.kb.healthcare.common.business.record.domain.entity.QRecord.record;
import static com.kb.healthcare.common.business.record.domain.entity.QSource.source;
import static com.kb.healthcare.common.business.user.domain.entity.QUser.user;

@RequiredArgsConstructor
public class RecordRepositoryCustomImpl implements RecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FindRecordResDto> findByRecordsByUserId(Long userId) {
        return queryFactory.select(
            new QFindRecordResDto(
                source.lastUpdate,
                source.memo,
                source.type,
                source.name,
                source.vender,
                source.productName,
                source.mode,
                source.recordKey,
                record.steps,
                record.periodFrom,
                record.periodTo,
                record.distanceValue,
                record.distanceUnit,
                record.caloriesValue,
                record.caloriesUnit
            )
        )
        .from(user)
        .innerJoin(source).on(user.id.eq(source.user.id))
        .innerJoin(record).on(source.id.eq(record.source.id))
        .where(user.id.eq(userId))
        .fetch();
    }

}
