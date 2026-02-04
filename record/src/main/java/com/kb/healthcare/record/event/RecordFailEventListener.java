package com.kb.healthcare.record.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.record.domain.entity.RecordFail;
import com.kb.healthcare.common.business.record.service.RecordFailService;
import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.business.user.service.UserService;
import com.kb.healthcare.common.exception.RecordGlobalException;
import com.kb.healthcare.common.utils.KBEnums;
import com.kb.healthcare.record.dto.request.RecordFailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RecordFailEventListener {

    private final UserService userService;
    private final RecordFailService recordFailService;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    public void handleRecordFailEvent(RecordFailEvent event) {

        try {
            User user = userService.findById(event.getUserId()).orElseThrow(() -> new RecordGlobalException(
                KBEnums.UserState.유효하지_않은_고객.getCode(),
                KBEnums.UserState.유효하지_않은_고객.getMessage()
            ));

            List<RecordFail> recordFails = new ArrayList<>();

            for (Record record : event.getFailedRecords()) {
                // Record를 Entry JSON으로 변환
                Map<String, Object> entryJson = Map.of(
                    "steps", record.getSteps(),
                    "periodFrom", record.getPeriodFrom().toString(),
                    "periodTo", record.getPeriodTo().toString(),
                    "distanceUnit", record.getDistanceUnit(),
                    "distanceValue", record.getDistanceValue(),
                    "caloriesUnit", record.getCaloriesUnit(),
                    "caloriesValue", record.getCaloriesValue()
                );

                recordFails.add(
                        RecordFail.builder()
                                .user(user)
                                .recordKey(event.getRecordKey())
                                .sourceId(event.getSourceId())
                                .entryJson(objectMapper.writeValueAsString(entryJson))
                                .status(KBEnums.RecordState.대기중.getCode())
                                .build()
                );

            }

            // DLQ 저장
            recordFailService.saveAll(recordFails);
            log.info("DLQ 저장 완료: {}건", recordFails.size());

        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("DLQ 저장 실패: {}건 - {}", event.getFailedRecords().size(), e.getMessage());
        }

    }

}
