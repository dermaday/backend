package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.OAuthAccount;
import com.likelion.dermaday.api.member.dto.request.OAuthLoginRequest;
import com.likelion.dermaday.api.member.dto.response.OAuthLoginResponse;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.member.repository.OAuthAccountRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import com.likelion.dermaday.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    @Transactional
    public OAuthLoginResponse loginOrCreate(OAuthLoginRequest request) {
        return oAuthAccountRepository
                .findByProviderAndProviderUserId(request.provider(), request.providerUserId())
                .map(account -> loginExisting(account, request.displayName()))
                .orElseGet(() -> createMember(request));
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER.getMessage()));
        member.withdraw();
    }

    private OAuthLoginResponse loginExisting(OAuthAccount account, String displayName) {
        Member member = account.getMember();
        member.login(displayName);
        return loginResponse(member, account);
    }

    private OAuthLoginResponse createMember(OAuthLoginRequest request) {
        Member member = memberRepository.save(Member.createUser(request.displayName()));
        OAuthAccount account = oAuthAccountRepository.save(
                OAuthAccount.create(member, request.provider(), request.providerUserId())
        );
        return loginResponse(member, account);
    }

    private OAuthLoginResponse loginResponse(Member member, OAuthAccount account) {
        return new OAuthLoginResponse(
                member.getId(),
                member.getDisplayName(),
                member.getRole(),
                account.getProvider()
        );
    }
}
