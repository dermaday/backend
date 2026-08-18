package com.likelion.dermaday.api.whs.domain;

import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "whs_mock_data",
        uniqueConstraints = @UniqueConstraint(name = "uk_whs_mock_key", columnNames = "mock_key")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WhsMockData extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mock_key", nullable = false, length = 50)
    private String mockKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    private WhsMockData(String mockKey, String payload) {
        this.mockKey = requireText(mockKey, "WHS mock key");
        this.payload = requireText(payload, "WHS mock payload");
    }

    public static WhsMockData create(String mockKey, String payload) {
        return new WhsMockData(mockKey, payload);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
