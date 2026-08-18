package com.likelion.dermaday.api.skin.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkinType {
    DRY("건성", "피부가 당기고 각질이 자주 생겨요"),
    NORMAL("중성", "유수분 밸런스가 적당해요"),
    OILY("지성", "피지와 유분기가 많고 번들거려요"),
    COMBINATION("복합성", "T존은 번들거리고 U존은 당겨요"),
    UNKNOWN("잘 모르겠어요", "피부 타입 진단을 위한 정보가 필요해요");

    private final String displayName;
    private final String description;
}
