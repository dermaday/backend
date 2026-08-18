package com.likelion.dermaday.api.treatment.dto.response;

import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TreatmentResponse(
        Long id,
        TreatmentRecordSource source,
        LocalDate latestTreatedOn,
        List<TreatmentItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TreatmentResponse from(TreatmentRecord record) {
        return new TreatmentResponse(
                record.getId(),
                record.getSource(),
                record.latestTreatedOn(),
                record.getItems().stream().map(TreatmentItemResponse::from).toList(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
