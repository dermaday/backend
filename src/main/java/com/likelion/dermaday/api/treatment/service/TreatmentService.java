package com.likelion.dermaday.api.treatment.service;

import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.report.service.ReportDataDeletionService;
import com.likelion.dermaday.api.treatment.domain.TreatmentItem;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.common.exception.BadRequestException;
import com.likelion.dermaday.common.exception.NotFoundException;
import com.likelion.dermaday.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreatmentService {

    private final TreatmentRecordRepository treatmentRecordRepository;
    private final MemberRepository memberRepository;
    private final CosmeticService cosmeticService;
    private final ReportDataDeletionService reportDataDeletionService;

    public List<TreatmentResponse> findAll(Long memberId) {
        return treatmentRecordRepository.findAllByMember_IdOrderByCreatedAtDesc(memberId).stream()
                .map(TreatmentResponse::from)
                .sorted(Comparator.comparing(TreatmentResponse::latestTreatedOn).reversed())
                .toList();
    }

    public TreatmentResponse find(Long memberId, Long recordId) {
        return TreatmentResponse.from(getOwnedRecord(memberId, recordId));
    }

    @Transactional
    public TreatmentResponse create(Long memberId, CreateTreatmentRequest request) {
        return create(memberId, request.items(), TreatmentRecordSource.MANUAL);
    }

    @Transactional
    public TreatmentResponse create(
            Long memberId,
            List<TreatmentItemRequest> itemRequests,
            TreatmentRecordSource source
    ) {
        validateNoDuplicateTypes(itemRequests);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER.getMessage()));
        List<TreatmentItem> items = itemRequests.stream()
                .map(item -> TreatmentItem.create(item.treatmentType(), item.treatedOn(), item.reaction()))
                .toList();
        TreatmentRecord saved = treatmentRecordRepository.save(TreatmentRecord.create(member, source, items));
        return TreatmentResponse.from(saved);
    }

    public Optional<TreatmentResponse> findBySource(Long memberId, TreatmentRecordSource source) {
        return treatmentRecordRepository.findFirstByMember_IdAndSourceOrderByCreatedAtAsc(memberId, source)
                .map(TreatmentResponse::from);
    }

    @Transactional
    public void delete(Long memberId, Long recordId) {
        TreatmentRecord record = getOwnedRecord(memberId, recordId);
        reportDataDeletionService.deleteByTreatmentRecord(memberId, recordId);
        cosmeticService.deleteAllByTreatmentRecord(memberId, recordId);
        treatmentRecordRepository.delete(record);
    }

    private TreatmentRecord getOwnedRecord(Long memberId, Long recordId) {
        return treatmentRecordRepository.findByIdAndMember_Id(recordId, memberId)
                .orElseThrow(() -> new NotFoundException("해당 시술 기록을 찾을 수 없습니다."));
    }

    private void validateNoDuplicateTypes(List<TreatmentItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("시술 항목을 하나 이상 입력해 주세요.");
        }
        if (new HashSet<>(items.stream().map(TreatmentItemRequest::treatmentType).toList()).size() != items.size()) {
            throw new BadRequestException("하나의 시술 기록에 같은 시술을 중복 등록할 수 없습니다.");
        }
    }
}
