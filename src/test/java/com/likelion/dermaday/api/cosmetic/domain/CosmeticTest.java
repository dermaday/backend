package com.likelion.dermaday.api.cosmetic.domain;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.treatment.domain.TreatmentItem;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CosmeticTest {

    private final TreatmentRecord treatmentRecord = TreatmentRecord.create(
            Member.createUser("회원"),
            TreatmentRecordSource.MANUAL,
            List.of(TreatmentItem.create(
                    TreatmentType.ULTHERA,
                    LocalDate.of(2026, 8, 1),
                    TreatmentReaction.COMFORTABLE
            ))
    );

    @Test
    void normalizesValuesAndProtectsIngredientCollection() {
        Set<IngredientType> ingredients = new LinkedHashSet<>(Set.of(IngredientType.RETINOL));

        Cosmetic cosmetic = Cosmetic.create(
                treatmentRecord,
                "  센텔라 스킨  ",
                CosmeticType.TONER_SKIN,
                ingredients,
                "  members/1/images/image.png  "
        );
        ingredients.add(IngredientType.AHA);

        assertEquals("센텔라 스킨", cosmetic.getName());
        assertEquals("members/1/images/image.png", cosmetic.getImageObjectKey());
        assertEquals(Set.of(IngredientType.RETINOL), cosmetic.getIngredients());
        assertThrows(UnsupportedOperationException.class, () -> cosmetic.getIngredients().clear());

        cosmetic.change(
                "크림",
                CosmeticType.LOTION_CREAM,
                Set.of(IngredientType.VITAMIN_C),
                " "
        );
        assertNull(cosmetic.getImageObjectKey());
        assertEquals(Set.of(IngredientType.VITAMIN_C), cosmetic.getIngredients());
    }

    @Test
    void rejectsInvalidRequiredValues() {
        assertThrows(NullPointerException.class, () -> Cosmetic.create(
                null, "제품", CosmeticType.OIL, Set.of(IngredientType.AHA), null
        ));
        assertThrows(IllegalArgumentException.class, () -> Cosmetic.create(
                treatmentRecord, " ", CosmeticType.OIL, Set.of(IngredientType.AHA), null
        ));
        assertThrows(IllegalArgumentException.class, () -> Cosmetic.create(
                treatmentRecord, "가".repeat(101), CosmeticType.OIL, Set.of(IngredientType.AHA), null
        ));
        assertThrows(NullPointerException.class, () -> Cosmetic.create(
                treatmentRecord, "제품", null, Set.of(IngredientType.AHA), null
        ));
        assertThrows(IllegalArgumentException.class, () -> Cosmetic.create(
                treatmentRecord, "제품", CosmeticType.OIL, Set.of(), null
        ));
    }
}
