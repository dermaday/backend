package com.likelion.dermaday.api.report.service;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.report.dto.ReportInput;
import com.likelion.dermaday.api.skin.domain.SkinProfile;
import com.likelion.dermaday.api.skin.repository.SkinProfileRepository;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportInputQueryService {

    private final TreatmentRecordRepository treatmentRecordRepository;
    private final SkinProfileRepository skinProfileRepository;
    private final CosmeticRepository cosmeticRepository;

    public ReportInput load(Long memberId, Long treatmentRecordId) {
        TreatmentRecord treatmentRecord = treatmentRecordRepository
                .findByIdAndMember_Id(treatmentRecordId, memberId)
                .orElseThrow(() -> new NotFoundException("해당 시술 기록을 찾을 수 없습니다."));
        SkinProfile skinProfile = skinProfileRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new NotFoundException("등록된 피부 타입이 없습니다."));

        return new ReportInput(
                treatmentRecord.getId(),
                skinProfile.getSkinType(),
                treatmentRecord.getItems().stream()
                        .map(item -> new ReportInput.Treatment(
                                item.getTreatmentType(),
                                item.getTreatedOn(),
                                item.getReaction()
                        ))
                        .toList(),
                cosmeticRepository
                        .findAllByTreatmentRecord_IdAndTreatmentRecord_Member_IdOrderByCreatedAtAsc(
                                treatmentRecordId,
                                memberId
                        )
                        .stream()
                        .map(this::toCosmeticInput)
                        .toList()
        );
    }

    private ReportInput.Cosmetic toCosmeticInput(Cosmetic cosmetic) {
        return new ReportInput.Cosmetic(
                cosmetic.getId(),
                cosmetic.getName(),
                cosmetic.getProductType(),
                Set.copyOf(cosmetic.getIngredients())
        );
    }
}
