package com.likelion.dermaday.api.skin.domain;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(
        name = "skin_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_skin_profile_member", columnNames = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkinProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkinType skinType;

    private SkinProfile(Member member, SkinType skinType) {
        this.member = Objects.requireNonNull(member, "Member must not be null");
        change(skinType);
    }

    public static SkinProfile create(Member member, SkinType skinType) {
        return new SkinProfile(member, skinType);
    }

    public void change(SkinType skinType) {
        this.skinType = Objects.requireNonNull(skinType, "Skin type must not be null");
    }
}
