package com.likelion.dermaday.api.cosmetic.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IngredientType {
    RETINOL("레티놀"),
    AHA("AHA"),
    BHA("BHA"),
    VITAMIN_C("비타민C"),
    GENERAL_COSMETIC("일반화장품");

    private final String displayName;
}
