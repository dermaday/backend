package com.likelion.dermaday.api.cosmetic.domain;

import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cosmetics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cosmetic extends BaseTimeEntity {

    private static final int MAX_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_record_id", nullable = false)
    private TreatmentRecord treatmentRecord;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CosmeticType productType;

    @ElementCollection
    @CollectionTable(
            name = "cosmetic_ingredients",
            joinColumns = @JoinColumn(name = "cosmetic_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "ingredient_type", nullable = false, length = 40)
    private Set<IngredientType> ingredients = new LinkedHashSet<>();

    @Column(length = 1024)
    private String imageObjectKey;

    private Cosmetic(
            TreatmentRecord treatmentRecord,
            String name,
            CosmeticType productType,
            Set<IngredientType> ingredients,
            String imageObjectKey
    ) {
        this.treatmentRecord = Objects.requireNonNull(treatmentRecord, "Treatment record must not be null");
        change(name, productType, ingredients, imageObjectKey);
    }

    public static Cosmetic create(
            TreatmentRecord treatmentRecord,
            String name,
            CosmeticType productType,
            Set<IngredientType> ingredients,
            String imageObjectKey
    ) {
        return new Cosmetic(treatmentRecord, name, productType, ingredients, imageObjectKey);
    }

    public void change(
            String name,
            CosmeticType productType,
            Set<IngredientType> ingredients,
            String imageObjectKey
    ) {
        this.name = validateName(name);
        this.productType = Objects.requireNonNull(productType, "Cosmetic type must not be null");
        this.ingredients.clear();
        this.ingredients.addAll(validateIngredients(ingredients));
        this.imageObjectKey = normalizeImageObjectKey(imageObjectKey);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cosmetic name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Cosmetic name must not exceed 100 characters");
        }
        return trimmed;
    }

    private Set<IngredientType> validateIngredients(Set<IngredientType> ingredients) {
        if (ingredients == null
                || ingredients.isEmpty()
                || ingredients.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one cosmetic ingredient is required");
        }
        return new LinkedHashSet<>(ingredients);
    }

    private String normalizeImageObjectKey(String imageObjectKey) {
        if (imageObjectKey == null || imageObjectKey.isBlank()) {
            return null;
        }
        return imageObjectKey.trim();
    }
}
