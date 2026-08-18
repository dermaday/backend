package com.likelion.dermaday.api.treatment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TreatmentType {
    ULTHERA("울쎄라", TreatmentCategory.LIFTING_ELASTICITY, null),
    SHURINK("슈링크", TreatmentCategory.LIFTING_ELASTICITY, null),
    INMODE("인모드", TreatmentCategory.LIFTING_ELASTICITY, null),
    OLIGIO("올라지오", TreatmentCategory.LIFTING_ELASTICITY, null),
    THERMAGE("써마지", TreatmentCategory.LIFTING_ELASTICITY, null),
    THREAD_LIFT("실리프팅", TreatmentCategory.LIFTING_ELASTICITY, null),

    PICO_TONING("피코토닝", TreatmentCategory.PIGMENT_LASER, null),
    LASER_TONING("레이저 토닝", TreatmentCategory.PIGMENT_LASER, null),
    IPL("IPL", TreatmentCategory.PIGMENT_LASER, null),
    FRAXEL("프락셀", TreatmentCategory.PIGMENT_LASER, null),
    SPOT_WART_REMOVAL("점 / 잡티 제거", TreatmentCategory.PIGMENT_LASER, null),

    AQUA_PEEL("아쿠아필", TreatmentCategory.PEELING_SKIN_TEXTURE, null),
    LALA_PEEL("라라필", TreatmentCategory.PEELING_SKIN_TEXTURE, null),
    ALADDIN_PEELING("알라딘 필링", TreatmentCategory.PEELING_SKIN_TEXTURE, null),
    PLAPPEEL_MILKPEEL("플라필 / 밀크필", TreatmentCategory.PEELING_SKIN_TEXTURE, null),
    POTENZA_MTS("포텐자 / MTS", TreatmentCategory.PEELING_SKIN_TEXTURE, null),

    REJURAN_HEALER("리쥬란 힐러", TreatmentCategory.SKIN_BOOSTER_NUTRITION, null),
    JUVLOOK("쥬베룩", TreatmentCategory.SKIN_BOOSTER_NUTRITION, null),
    EXOSOME("엑소좀", TreatmentCategory.SKIN_BOOSTER_NUTRITION, null),
    HYDRATION_INJECTION("물광주사", TreatmentCategory.SKIN_BOOSTER_NUTRITION, null),
    CHANEL_INJECTION("샤넬주사", TreatmentCategory.SKIN_BOOSTER_NUTRITION, "NCTF"),

    SQUARE_JAW_BODY_BOTOX("사각턱 / 바디 보톡스", TreatmentCategory.BOTOX_FILLER, null),
    WRINKLE_BOTOX("주름 보톡스", TreatmentCategory.BOTOX_FILLER, null),
    LIP_CHIN_FILLER("입술 / 턱끝 필러", TreatmentCategory.BOTOX_FILLER, null),
    VOLUME_FILLER("볼륨 필러", TreatmentCategory.BOTOX_FILLER, "이마 · 앞광대 · 팔자"),
    CONTOUR_INJECTION("윤곽주사", TreatmentCategory.BOTOX_FILLER, null);

    private final String displayName;
    private final TreatmentCategory category;
    private final String description;
}
