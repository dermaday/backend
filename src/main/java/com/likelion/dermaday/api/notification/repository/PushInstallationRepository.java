package com.likelion.dermaday.api.notification.repository;

import com.likelion.dermaday.api.notification.domain.PushInstallation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushInstallationRepository extends JpaRepository<PushInstallation, Long> {

    Optional<PushInstallation> findByFid(String fid);

    List<PushInstallation> findAllByMember_IdOrderByCreatedAtAsc(Long memberId);

    void deleteByMember_IdAndFid(Long memberId, String fid);

    void deleteAllByFidIn(List<String> fids);

    void deleteAllByMember_Id(Long memberId);
}
