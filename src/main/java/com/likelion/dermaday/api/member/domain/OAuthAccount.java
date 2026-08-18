package com.likelion.dermaday.api.member.domain;

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

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_account_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_oauth_account_member",
                        columnNames = "member_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseTimeEntity {

    private static final int MAX_PROVIDER_USER_ID_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column
    private LocalDateTime deletedAt;

    private OAuthAccount(Member member, OAuthProvider provider, String providerUserId) {
        this.member = requireMember(member);
        this.provider = requireProvider(provider);
        this.providerUserId = validateProviderUserId(providerUserId);
    }

    public static OAuthAccount create(Member member, OAuthProvider provider, String providerUserId) {
        return new OAuthAccount(member, provider, providerUserId);
    }

    public void withdraw() {
        this.member = null;
        this.deletedAt = LocalDateTime.now();
    }

    public void reactivate(Member member) {
        this.member = requireMember(member);
        this.deletedAt = null;
    }

    public boolean isWithdrawn() {
        return this.deletedAt != null || this.member == null;
    }

    private static Member requireMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member must not be null");
        }
        return member;
    }

    private static OAuthProvider requireProvider(OAuthProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("OAuth provider must not be null");
        }
        return provider;
    }

    private static String validateProviderUserId(String providerUserId) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("OAuth provider user id must not be blank");
        }
        if (providerUserId.length() > MAX_PROVIDER_USER_ID_LENGTH) {
            throw new IllegalArgumentException("OAuth provider user id must not exceed 255 characters");
        }
        return providerUserId;
    }
}
