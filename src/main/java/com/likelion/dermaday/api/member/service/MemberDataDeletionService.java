package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
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

    public void deleteAll(Long memberId) {
        cosmeticService.deleteAllByMember(memberId);
        treatmentRecordRepository.deleteAllByMember_Id(memberId);
        skinProfileRepository.deleteByMember_Id(memberId);
    }
}
