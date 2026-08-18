package com.likelion.dermaday.api.treatment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "treatment_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_treatment_item_record_type",
                columnNames = {"treatment_record_id", "treatment_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_record_id", nullable = false)
    private TreatmentRecord treatmentRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_type", nullable = false, length = 50)
    private TreatmentType treatmentType;

    @Column(nullable = false)
    private LocalDate treatedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TreatmentReaction reaction;

    private TreatmentItem(TreatmentType treatmentType, LocalDate treatedOn, TreatmentReaction reaction) {
        this.treatmentType = Objects.requireNonNull(treatmentType, "Treatment type must not be null");
        this.treatedOn = Objects.requireNonNull(treatedOn, "Treated date must not be null");
        this.reaction = Objects.requireNonNull(reaction, "Treatment reaction must not be null");
    }

    public static TreatmentItem create(
            TreatmentType treatmentType,
            LocalDate treatedOn,
            TreatmentReaction reaction
    ) {
        return new TreatmentItem(treatmentType, treatedOn, reaction);
    }

    void attachTo(TreatmentRecord treatmentRecord) {
        this.treatmentRecord = Objects.requireNonNull(treatmentRecord, "Treatment record must not be null");
    }
}
