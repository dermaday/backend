package com.likelion.dermaday.api.cosmetic.service;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.request.UpdateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.image.event.MemberImagesDeletionRequested;
import com.likelion.dermaday.api.image.service.ImageStorageService;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CosmeticService {

    private final CosmeticRepository cosmeticRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;
    private final ImageStorageService imageStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public List<CosmeticResponse> findAll(Long memberId, Long treatmentRecordId) {
        ensureOwnedRecord(memberId, treatmentRecordId);
        return cosmeticRepository
                .findAllByTreatmentRecord_IdAndTreatmentRecord_Member_IdOrderByCreatedAtAsc(
                        treatmentRecordId,
                        memberId
                )
                .stream()
                .map(CosmeticResponse::from)
                .toList();
    }

    public CosmeticResponse find(Long memberId, Long cosmeticId) {
        return CosmeticResponse.from(getOwnedCosmetic(memberId, cosmeticId));
    }

    @Transactional
    public CosmeticResponse create(Long memberId, CreateCosmeticRequest request) {
        TreatmentRecord treatmentRecord = ensureOwnedRecord(memberId, request.treatmentRecordId());
        validateImage(memberId, request.imageObjectKey());
        Cosmetic cosmetic = Cosmetic.create(
                treatmentRecord,
                request.name(),
                request.productType(),
                request.ingredients(),
                request.imageObjectKey()
        );
        return CosmeticResponse.from(cosmeticRepository.save(cosmetic));
    }

    @Transactional
    public CosmeticResponse update(Long memberId, Long cosmeticId, UpdateCosmeticRequest request) {
        Cosmetic cosmetic = getOwnedCosmetic(memberId, cosmeticId);
        validateImage(memberId, request.imageObjectKey());
        String previousImageKey = cosmetic.getImageObjectKey();
        cosmetic.change(request.name(), request.productType(), request.ingredients(), request.imageObjectKey());
        if (!Objects.equals(previousImageKey, cosmetic.getImageObjectKey())) {
            requestImageDeletion(memberId, previousImageKey);
        }
        return CosmeticResponse.from(cosmetic);
    }

    @Transactional
    public void delete(Long memberId, Long cosmeticId) {
        Cosmetic cosmetic = getOwnedCosmetic(memberId, cosmeticId);
        requestImageDeletion(memberId, cosmetic.getImageObjectKey());
        cosmeticRepository.delete(cosmetic);
    }

    @Transactional
    public void deleteAllByTreatmentRecord(Long memberId, Long treatmentRecordId) {
        List<String> imageKeys = cosmeticRepository.findImageObjectKeysByTreatmentRecord(treatmentRecordId, memberId);
        requestImageDeletion(memberId, imageKeys);
        cosmeticRepository.deleteAllByTreatmentRecord_IdAndTreatmentRecord_Member_Id(treatmentRecordId, memberId);
    }

    @Transactional
    public void deleteAllByMember(Long memberId) {
        List<String> imageKeys = cosmeticRepository.findImageObjectKeysByMemberId(memberId);
        requestImageDeletion(memberId, imageKeys);
        cosmeticRepository.deleteAllByTreatmentRecord_Member_Id(memberId);
    }

    private TreatmentRecord ensureOwnedRecord(Long memberId, Long treatmentRecordId) {
        return treatmentRecordRepository.findByIdAndMember_Id(treatmentRecordId, memberId)
                .orElseThrow(() -> new NotFoundException("해당 시술 기록을 찾을 수 없습니다."));
    }

    private Cosmetic getOwnedCosmetic(Long memberId, Long cosmeticId) {
        return cosmeticRepository.findByIdAndTreatmentRecord_Member_Id(cosmeticId, memberId)
                .orElseThrow(() -> new NotFoundException("해당 화장품을 찾을 수 없습니다."));
    }

    private void validateImage(Long memberId, String imageObjectKey) {
        if (imageObjectKey != null && !imageObjectKey.isBlank()) {
            imageStorageService.validateReadableObject(memberId, imageObjectKey);
        }
    }

    private void requestImageDeletion(Long memberId, String imageObjectKey) {
        if (imageObjectKey != null && !imageObjectKey.isBlank()) {
            eventPublisher.publishEvent(MemberImagesDeletionRequested.of(memberId, imageObjectKey));
        }
    }

    private void requestImageDeletion(Long memberId, List<String> imageObjectKeys) {
        if (!imageObjectKeys.isEmpty()) {
            eventPublisher.publishEvent(new MemberImagesDeletionRequested(memberId, imageObjectKeys));
        }
    }
}
