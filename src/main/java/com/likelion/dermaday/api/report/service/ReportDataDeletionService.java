package com.likelion.dermaday.api.report.service;

import com.likelion.dermaday.api.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportDataDeletionService {

    private final ReportRepository reportRepository;

    public void deleteAllByMember(Long memberId) {
        reportRepository.deleteAllByMemberId(memberId);
    }

    public void deleteByTreatmentRecord(Long memberId, Long treatmentRecordId) {
        reportRepository.deleteByMemberIdAndTreatmentRecordId(memberId, treatmentRecordId);
    }
}
