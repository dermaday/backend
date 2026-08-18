package com.likelion.dermaday.api.member.domain;

import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    private static final int MAX_DISPLAY_NAME_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column
    private LocalDateTime withdrawnAt;

    private Member(String displayName) {
        this.displayName = validateDisplayName(displayName);
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member createUser(String displayName) {
        return new Member(displayName);
    }

    public void login(String latestDisplayName) {
        this.displayName = validateDisplayName(latestDisplayName);
        this.status = MemberStatus.ACTIVE;
        this.withdrawnAt = null;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    private static String validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name must not be blank");
        }
        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("Display name must not exceed 100 characters");
        }
        return displayName;
    }
}
