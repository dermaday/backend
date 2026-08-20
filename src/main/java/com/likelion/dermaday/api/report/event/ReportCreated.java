package com.likelion.dermaday.api.report.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record ReportCreated(
        String reportId,
        Long memberId,
        Long treatmentRecordId,
        Instant createdAt,
        List<ProductSnapshot> products
) {
    public ReportCreated {
        Objects.requireNonNull(reportId, "Report id must not be null");
        Objects.requireNonNull(memberId, "Member id must not be null");
        Objects.requireNonNull(treatmentRecordId, "Treatment record id must not be null");
        Objects.requireNonNull(createdAt, "Created time must not be null");
        products = List.copyOf(Objects.requireNonNull(products, "Products must not be null"));
    }

    public record ProductSnapshot(
            Long cosmeticId,
            String productName,
            LocalDate unlockDate
    ) {
        public ProductSnapshot {
            Objects.requireNonNull(cosmeticId, "Cosmetic id must not be null");
            Objects.requireNonNull(productName, "Product name must not be null");
        }
    }
}
