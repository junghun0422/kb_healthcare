package com.kb.healthcare.record.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.record.domain.entity.Source;
import com.kb.healthcare.common.business.record.dto.FindRecordResDto;
import com.kb.healthcare.common.business.record.service.RecordService;
import com.kb.healthcare.common.business.record.service.SourceService;
import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.business.user.domain.entity.UserIdentifier;
import com.kb.healthcare.common.business.user.service.UserIdentifierService;
import com.kb.healthcare.common.business.user.service.UserService;
import com.kb.healthcare.common.dto.response.ApiResponse;
import com.kb.healthcare.common.exception.RecordGlobalException;
import com.kb.healthcare.record.dto.request.RecordFailEvent;
import com.kb.healthcare.record.dto.request.RecordRequestDto;
import com.kb.healthcare.record.dto.response.RecordResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;import java.util.stream.Collectors;

import static com.kb.healthcare.common.utils.KBEnums.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordApiService {

    private final UserService userService;
    private final UserIdentifierService userIdentifierService;
    private final SourceService sourceService;
    private final RecordService recordService;

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;

    public ApiResponse<Void> save(Jwt jwt, RecordRequestDto request) {

        String userId = jwt.getClaimAsString("userId");
        String recordKey = request.recordkey();

        User user = userService.findById(Long.valueOf(userId)).orElseThrow(() -> new RecordGlobalException(
                UserState.유효하지_않은_고객.getCode(),
                UserState.유효하지_않은_고객.getMessage()
        ));

        UserIdentifier userIdentifier = userIdentifierService.findByUserIdAndRecordKey(Long.parseLong(userId), recordKey);
        if (Objects.isNull(userIdentifier)) {
            userIdentifierService.save(UserIdentifier.builder()
                    .user(user)
                    .recordKey(recordKey)
                    .build()
            );
        }

        final Source source = Optional.ofNullable(
                sourceService.findByRecordKeyAndMode(recordKey, request.data().source().mode())
        ).orElseGet(() -> {
            RecordRequestDto.Source requestSource = request.data().source();
            return sourceService.save(Source.builder()
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
        recordService.saveAllInNewTransaction(records);

        return ApiResponse.<Void>builder().build();
    }

    public ApiResponse<Void> saveStreaming(Jwt jwt, HttpServletRequest request) throws IOException {

        String userId = jwt.getClaimAsString("userId");

        // User 조회
        User user = userService.findById(Long.valueOf(userId)).orElseThrow(() -> new RecordGlobalException(
            UserState.유효하지_않은_고객.getCode(),
            UserState.유효하지_않은_고객.getMessage()
        ));

        // JSON 파싱
        Map<String, Object> rootMap = objectMapper.readValue(
                request.getInputStream(),
                new TypeReference<>() {}
        );

        String recordKey = (String) rootMap.get("recordkey");
        LocalDateTime lastUpdate = parseDateTime((String) rootMap.get("lastUpdate"));

        Map<String, Object> dataMap = (Map<String, Object>) rootMap.get("data");
        String memo = (String) dataMap.get("memo");
        RecordRequestDto.Source sourceDto = objectMapper.convertValue(
                dataMap.get("source"),
                RecordRequestDto.Source.class
        );

        // UserIdentifier 처리
        if (userIdentifierService.findByUserIdAndRecordKey(Long.parseLong(userId), recordKey) == null) {
            userIdentifierService.save(
                    UserIdentifier.builder().user(user).recordKey(recordKey).build()
            );
        }

        // Source 처리
        Source source = sourceService.findByRecordKeyAndMode(recordKey, sourceDto.mode());

        if (source == null) {
            source = sourceService.save(
                    Source.builder()
                            .recordKey(recordKey)
                            .mode(sourceDto.mode())
                            .vender(sourceDto.product().vender())
                            .productName(sourceDto.product().name())
                            .type(sourceDto.type())
                            .name(sourceDto.name())
                            .memo(memo)
                            .lastUpdate(lastUpdate)
                            .build()
            );
        }

        // entries 배치 저장
        List<Map<String, Object>> entriesList = (List<Map<String, Object>>) dataMap.get("entries");
        List<Record> batch = new ArrayList<>(100);
        int totalCount = 0;

        for (Map<String, Object> entryMap : entriesList) {
            RecordRequestDto.Entry entry = objectMapper.convertValue(entryMap, RecordRequestDto.Entry.class);

            batch.add(Record.builder()
                    .source(source)
                    .steps(Math.round(entry.steps()))
                    .periodFrom(entry.period().from())
                    .periodTo(entry.period().to())
                    .distanceUnit(entry.distance().unit())
                    .distanceValue(entry.distance().value())
                    .caloriesUnit(entry.calories().unit())
                    .caloriesValue(entry.calories().value())
                    .build());

            totalCount++;

            if (batch.size() >= 100) {
                try {
                    recordService.saveAllInNewTransaction(batch);
                    log.info("배치 저장: {}건 (누적: {}건)", batch.size(), totalCount);
                } catch (Exception e) {
                    log.error("배치 저장 실패 → DLQ 저장: {}건", batch.size());
                    eventPublisher.publishEvent(
                        new RecordFailEvent(
                            user.getId(),
                            recordKey,
                            source.getId(),
                            new ArrayList<>(batch),
                            e.getMessage()
                        )
                    );
                }

                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            try {
                recordService.saveAllInNewTransaction(batch);
            } catch (Exception e) {
                eventPublisher.publishEvent(
                    new RecordFailEvent(
                        user.getId(),
                        recordKey,
                        source.getId(),
                        new ArrayList<>(batch),
                        e.getMessage()
                    )
                );
            }

        }

        log.info("총 {}건 저장 완료", totalCount);
        return ApiResponse.<Void>builder().build();
    }

    public ApiResponse<List<RecordResponseDto>> getRecord(Jwt jwt){
        String userId = jwt.getClaimAsString("userId");

        // User 조회
        User user = userService.findById(Long.valueOf(userId)).orElseThrow(() -> new RecordGlobalException(
                UserState.유효하지_않은_고객.getCode(),
                UserState.유효하지_않은_고객.getMessage()
        ));

        // QueryDSL로 flat하게 조회
        List<FindRecordResDto> flatRecords = recordService.findByRecordsByUserId(Long.parseLong(userId));

        // recordKey 기준으로 그룹핑
//        Map<String, List<FindRecordResDto>> groupedByRecordKey = flatRecords.stream()
//                                                                    .collect(Collectors.groupingBy(FindRecordResDto::recordKey));

        Map<String, List<FindRecordResDto>> groupedByRecordKey = flatRecords.stream()
                .collect(Collectors.groupingBy(FindRecordResDto::recordKey));

        List<RecordResponseDto> responses = groupedByRecordKey.entrySet().stream()
                .map(entry -> convertToRecordResponseDto(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ApiResponse.<List<RecordResponseDto>>builder()
                .data(responses)
                .build();
    }

    private RecordResponseDto convertToRecordResponseDto(String recordKey, List<FindRecordResDto> records) {
        if (records.isEmpty()) {
            return null;
        }

        // 첫 번째 레코드에서 공통 정보 추출
        FindRecordResDto first = records.get(0);

        // entries 리스트 생성 (여러 개!)
        List<RecordResponseDto.Entry> entries = records.stream()
                .map(record -> new RecordResponseDto.Entry(
                        new RecordResponseDto.Period(
                            record.periodFrom(),
                            record.periodTo()
                        ),
                        new RecordResponseDto.Distance(
                            record.distanceUnit(),
                            record.distanceValue()
                        ),
                        new RecordResponseDto.Calorie(
                            record.caloriesUnit(),
                            record.caloriesValue()
                        ),
                        record.steps()
                ))
                .collect(Collectors.toList());

        // Source 생성
        RecordResponseDto.Source source = new RecordResponseDto.Source(
                first.mode(),
                new RecordResponseDto.Product(
                    first.productName(),
                    first.vender()
                ),
                first.name(),
                first.type()
        );

        // Data 생성
        RecordResponseDto.Data data = new RecordResponseDto.Data(
                first.memo(),
                entries,  // ← 여기가 List!
                source
        );

        // RecordResponseDto 생성
        return new RecordResponseDto(
                recordKey,
                data,
                first.localDateTime(),
                first.type()
        );
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            String cleaned = dateTimeStr.replace(" +0000", "").trim();

            if (cleaned.contains("T")) {
                return LocalDateTime.parse(cleaned.replace("+0000", ""));
            }

            return LocalDateTime.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateTimeStr, e);
            return LocalDateTime.now();
        }
    }

}