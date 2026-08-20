package com.likelion.dermaday.api.report.domain;

import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reports_member_treatment",
                columnNames = {"member_id", "treatment_record_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "treatment_record_id", nullable = false)
    private Long treatmentRecordId;

    @Lob
    @Column(nullable = false)
    private String inputPayload;

    private Report(String id, Long memberId, Long treatmentRecordId, String inputPayload) {
        this.id = id;
        this.memberId = memberId;
        this.treatmentRecordId = treatmentRecordId;
        this.inputPayload = inputPayload;
    }

    public static Report create(String id, Long memberId, Long treatmentRecordId, String inputPayload) {
        return new Report(id, memberId, treatmentRecordId, inputPayload);
    }
}
