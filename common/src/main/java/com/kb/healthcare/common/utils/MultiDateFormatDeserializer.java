package com.kb.healthcare.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MultiDateFormatDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        String str = jsonParser.getText().trim();

        List<DateTimeFormatter> localFormatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
        );
        List<DateTimeFormatter> offsetFormatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        // 1) LocalDateTime 포맷 시도
        for (DateTimeFormatter formatter : localFormatters) {
            try {
                return LocalDateTime.parse(str, formatter);
            } catch (Exception ignored) {}
        }

        // 2) OffsetDateTime 포맷 시도
        for (DateTimeFormatter formatter : offsetFormatters) {
            try {
                return OffsetDateTime.parse(str, formatter).toLocalDateTime();
            } catch (Exception ignored) {}
        }

        throw new RuntimeException("Cannot parse date: " + str);
    }

}