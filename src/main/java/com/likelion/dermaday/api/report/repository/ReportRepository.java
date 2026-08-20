package com.likelion.dermaday.api.report.repository;

import com.likelion.dermaday.api.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, String> {

    Optional<Report> findByIdAndMemberId(String id, Long memberId);
}
