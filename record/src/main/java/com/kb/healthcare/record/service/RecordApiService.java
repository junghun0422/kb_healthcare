package com.kb.healthcare.record.service;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.record.domain.entity.Source;
import com.kb.healthcare.common.business.record.service.RecordService;
import com.kb.healthcare.common.business.record.service.SourceService;
import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.business.user.domain.entity.UserIdentifier;
import com.kb.healthcare.common.business.user.service.UserIdentifierService;
import com.kb.healthcare.common.business.user.service.UserService;
import com.kb.healthcare.common.exception.RecordGlobalException;
import com.kb.healthcare.common.utils.UserEnums;
import com.kb.healthcare.record.dto.request.RecordRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordApiService {

    private final UserService userService;
    private final UserIdentifierService userIdentifierService;
    private final SourceService sourceService;
    private final RecordService recordService;

    public com.kb.healthcare.common.dto.response.ApiResponse<Void> save(Jwt jwt, RecordRequestDto request) {

        String userId = jwt.getClaimAsString("userId");
        String recordKey = request.recordkey();

        User user = userService.findById(Long.valueOf(userId)).orElseThrow(() -> new RecordGlobalException(
            UserEnums.State.유효하지_않은_고객.getCode(),
            UserEnums.State.유효하지_않은_고객.getMessage()
        ));

        UserIdentifier userIdentifier = userIdentifierService.findByUserIdAndRecordKey(Long.parseLong(userId), recordKey);
        if(Objects.isNull(userIdentifier)){
            userIdentifierService.save(UserIdentifier.builder()
                                                        .user(user)
                                                        .recordKey(recordKey)
                                                    .build()
            );
        }

        final Source source = Optional.ofNullable(
                sourceService.findByUserIdAndRecordKeyAndMode(
                        Long.parseLong(userId),
                        recordKey,
                        request.data().source().mode()
                )
        ).orElseGet(() -> {
            RecordRequestDto.Source requestSource = request.data().source();
            return sourceService.save(Source.builder()
                                                    .user(user)
                                                    .recordKey(recordKey)
                                                    .mode(requestSource.mode())
                                                    .vender(requestSource.product().vender())
                                                    .productName(requestSource.product().name())
                                                    .type(requestSource.type())
                                                    .name(requestSource.name())
                                                    .memo(request.data().memo())
                                                    .lastUpdate(request.lastUpdate())
                                                .build()
            );
        });
        List<com.kb.healthcare.common.business.record.domain.entity.Record> records = request.data().entries().stream()
                .map(entry -> Record.builder()
                            .user(user)
                            .source(source)
                            .steps(Math.round(entry.steps()))
                            .periodFrom(entry.period().from())
                            .periodTo(entry.period().to())
                            .distanceUnit(entry.distance().unit())
                            .distanceValue(entry.distance().value())
                            .caloriesUnit(entry.calories().unit())
                            .caloriesValue(entry.calories().value())
                        .build()
                )
                .toList();
        recordService.saveAll(records);

        return com.kb.healthcare.common.dto.response.ApiResponse.<Void>builder().build();
    }
}
