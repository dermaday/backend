package com.likelion.dermaday.api.skin.repository;

import com.likelion.dermaday.api.skin.domain.SkinProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkinProfileRepository extends JpaRepository<SkinProfile, Long> {

    Optional<SkinProfile> findByMember_Id(Long memberId);

    void deleteByMember_Id(Long memberId);
}
