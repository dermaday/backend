package com.likelion.dermaday.api.treatment.repository;

import com.likelion.dermaday.api.treatment.domain.TreatmentRecord;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentRecordRepository extends JpaRepository<TreatmentRecord, Long> {

    @EntityGraph(attributePaths = "items")
    List<TreatmentRecord> findAllByMember_IdOrderByCreatedAtDesc(Long memberId);

    @EntityGraph(attributePaths = "items")
    Optional<TreatmentRecord> findByIdAndMember_Id(Long id, Long memberId);

    boolean existsByIdAndMember_Id(Long id, Long memberId);

    @EntityGraph(attributePaths = "items")
    Optional<TreatmentRecord> findFirstByMember_IdAndSourceOrderByCreatedAtAsc(
            Long memberId,
            TreatmentRecordSource source
    );

    void deleteAllByMember_Id(Long memberId);
}
