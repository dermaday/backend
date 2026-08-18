package com.likelion.dermaday.api.treatment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TreatmentReaction {
    COMFORTABLE("편안해요"),
    IRRITATED("자극이 있어요");

    private final String displayName;
}
