package com.likelion.dermaday.api.member.repository;

import com.likelion.dermaday.api.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
