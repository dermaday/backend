package com.likelion.dermaday.api.whs.repository;

import com.likelion.dermaday.api.whs.domain.WhsMockData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhsMockDataRepository extends JpaRepository<WhsMockData, Long> {

    boolean existsByMockKey(String mockKey);

    Optional<WhsMockData> findByMockKey(String mockKey);
}
