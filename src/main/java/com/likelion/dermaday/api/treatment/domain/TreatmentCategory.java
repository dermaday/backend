package com.likelion.dermaday.api.treatment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TreatmentCategory {
    LIFTING_ELASTICITY("리프팅 · 탄력"),
    PIGMENT_LASER("색소 · 레이저"),
    PEELING_SKIN_TEXTURE("필링 · 피부결"),
    SKIN_BOOSTER_NUTRITION("스킨부스터 · 영양"),
    BOTOX_FILLER("보톡스 · 필러");

    private final String displayName;
}
