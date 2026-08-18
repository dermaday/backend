package com.likelion.dermaday.api.whs.dto.response;

import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.skin.dto.response.SkinProfileResponse;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;

import java.util.List;

public record WhsImportResponse(
        boolean alreadyImported,
        SkinProfileResponse skinProfile,
        TreatmentResponse treatmentRecord,
        List<CosmeticResponse> cosmetics
) {
}
