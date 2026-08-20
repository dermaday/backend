package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.notification.service.PushDataDeletionService;
import com.likelion.dermaday.api.report.service.ReportDataDeletionService;
import com.likelion.dermaday.api.skin.repository.SkinProfileRepository;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberDataDeletionService {

    private final CosmeticService cosmeticService;
    private final TreatmentRecordRepository treatmentRecordRepository;
    private final SkinProfileRepository skinProfileRepository;
    private final PushDataDeletionService pushDataDeletionService;
    private final ReportDataDeletionService reportDataDeletionService;

    public void deleteAll(Long memberId) {
        pushDataDeletionService.deleteAllByMember(memberId);
        reportDataDeletionService.deleteAllByMember(memberId);
        cosmeticService.deleteAllByMember(memberId);
        treatmentRecordRepository.deleteAllByMember_Id(memberId);
        skinProfileRepository.deleteByMember_Id(memberId);
    }
}
