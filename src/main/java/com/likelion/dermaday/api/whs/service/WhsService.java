package com.likelion.dermaday.api.whs.service;

import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.skin.dto.response.SkinProfileResponse;
import com.likelion.dermaday.api.skin.service.SkinProfileService;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
import com.likelion.dermaday.api.whs.config.WhsMockDataInitializer;
import com.likelion.dermaday.api.whs.domain.WhsMockData;
import com.likelion.dermaday.api.whs.dto.WhsPayload;
import com.likelion.dermaday.api.whs.dto.response.WhsImportResponse;
import com.likelion.dermaday.api.whs.dto.response.WhsResponse;
import com.likelion.dermaday.api.whs.repository.WhsMockDataRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WhsService {

    private final WhsMockDataRepository whsMockDataRepository;
    private final ObjectMapper objectMapper;
    private final SkinProfileService skinProfileService;
    private final TreatmentService treatmentService;
    private final CosmeticService cosmeticService;

    public WhsResponse preview(String memberName) {
        return WhsResponse.from(memberName, loadPayload());
    }

    @Transactional
    public WhsImportResponse importData(Long memberId) {
        return treatmentService.findBySource(memberId, TreatmentRecordSource.WHS_MOCK)
                .map(existing -> existingImport(memberId, existing))
                .orElseGet(() -> importNew(memberId));
    }

    private WhsImportResponse importNew(Long memberId) {
        WhsPayload payload = loadPayload();
        SkinProfileResponse skinProfile = skinProfileService.upsert(memberId, payload.skinType());
        List<TreatmentItemRequest> itemRequests = payload.treatments().stream()
                .map(item -> new TreatmentItemRequest(
                        item.treatmentType(),
                        item.treatedOn(),
                        item.reaction()
                ))
                .toList();
        TreatmentResponse treatment = treatmentService.create(
                memberId,
                itemRequests,
                TreatmentRecordSource.WHS_MOCK
        );

        List<CosmeticResponse> cosmetics = new ArrayList<>();
        for (WhsPayload.Cosmetic cosmetic : payload.cosmetics()) {
            cosmetics.add(cosmeticService.create(memberId, new CreateCosmeticRequest(
                    treatment.id(),
                    cosmetic.name(),
                    cosmetic.productType(),
                    cosmetic.ingredients(),
                    cosmetic.imageObjectKey()
            )));
        }
        return new WhsImportResponse(false, skinProfile, treatment, List.copyOf(cosmetics));
    }

    private WhsImportResponse existingImport(Long memberId, TreatmentResponse treatment) {
        return new WhsImportResponse(
                true,
                skinProfileService.find(memberId),
                treatment,
                cosmeticService.findAll(memberId, treatment.id())
        );
    }

    private WhsPayload loadPayload() {
        WhsMockData mockData = whsMockDataRepository
                .findByMockKey(WhsMockDataInitializer.DEFAULT_MOCK_KEY)
                .orElseThrow(() -> new NotFoundException("WHS mock 데이터를 찾을 수 없습니다."));
        return objectMapper.readValue(mockData.getPayload(), WhsPayload.class);
    }
}
