package com.likelion.dermaday.api.member.repository;

import com.likelion.dermaday.api.member.domain.OAuthAccount;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    @EntityGraph(attributePaths = "member")
    Optional<OAuthAccount> findByMember_Id(Long memberId);
}
