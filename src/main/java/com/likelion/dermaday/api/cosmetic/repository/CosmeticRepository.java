package com.likelion.dermaday.api.cosmetic.repository;

import com.likelion.dermaday.api.cosmetic.domain.Cosmetic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CosmeticRepository extends JpaRepository<Cosmetic, Long> {

    @EntityGraph(attributePaths = {"ingredients", "treatmentRecord"})
    List<Cosmetic> findAllByTreatmentRecord_IdAndTreatmentRecord_Member_IdOrderByCreatedAtAsc(
            Long treatmentRecordId,
            Long memberId
    );

    @EntityGraph(attributePaths = {"ingredients", "treatmentRecord"})
    Optional<Cosmetic> findByIdAndTreatmentRecord_Member_Id(Long id, Long memberId);

    @Query("""
            select c.imageObjectKey
            from Cosmetic c
            where c.treatmentRecord.id = :recordId
              and c.treatmentRecord.member.id = :memberId
              and c.imageObjectKey is not null
            """)
    List<String> findImageObjectKeysByTreatmentRecord(
            @Param("recordId") Long recordId,
            @Param("memberId") Long memberId
    );

    @Query("""
            select c.imageObjectKey
            from Cosmetic c
            where c.treatmentRecord.member.id = :memberId
              and c.imageObjectKey is not null
            """)
    List<String> findImageObjectKeysByMemberId(@Param("memberId") Long memberId);

    void deleteAllByTreatmentRecord_IdAndTreatmentRecord_Member_Id(Long treatmentRecordId, Long memberId);

    void deleteAllByTreatmentRecord_Member_Id(Long memberId);
}
