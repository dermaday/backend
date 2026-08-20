package com.likelion.dermaday.api.notification.domain;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(
        name = "push_installations",
        uniqueConstraints = @UniqueConstraint(name = "uk_push_installations_fid", columnNames = "fid")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushInstallation extends BaseTimeEntity {

    private static final int MAX_FID_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = MAX_FID_LENGTH)
    private String fid;

    private PushInstallation(Member member, String fid) {
        this.member = Objects.requireNonNull(member, "Member must not be null");
        this.fid = validateFid(fid);
    }

    public static PushInstallation create(Member member, String fid) {
        return new PushInstallation(member, fid);
    }

    public void assignTo(Member member) {
        this.member = Objects.requireNonNull(member, "Member must not be null");
    }

    private static String validateFid(String fid) {
        if (fid == null || fid.isBlank()) {
            throw new IllegalArgumentException("FID must not be blank");
        }
        String trimmed = fid.trim();
        if (trimmed.length() > MAX_FID_LENGTH) {
            throw new IllegalArgumentException("FID must not exceed 255 characters");
        }
        return trimmed;
    }
}
