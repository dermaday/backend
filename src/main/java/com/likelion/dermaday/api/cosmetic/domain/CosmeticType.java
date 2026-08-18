package com.likelion.dermaday.api.cosmetic.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CosmeticType {
    TONER_SKIN("토너 · 스킨"),
    ESSENCE_AMPOULE_SERUM("에센스 · 앰플 · 세럼"),
    LOTION_CREAM("로션 · 크림"),
    OIL("오일");

    private final String displayName;
}
