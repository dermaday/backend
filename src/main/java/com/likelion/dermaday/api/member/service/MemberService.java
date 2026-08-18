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
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final MemberDataDeletionService memberDataDeletionService;

    public OAuthLoginResponse loginOrCreate(OAuthLoginRequest request) {
        return oAuthAccountRepository
                .findByProviderAndProviderUserId(request.provider(), request.providerUserId())
                .map(account -> loginExisting(account, request.displayName()))
                .orElseGet(() -> createMember(request));
    }

    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER.getMessage()));
        OAuthAccount account = oAuthAccountRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new NotFoundException("회원의 OAuth2 계정을 찾을 수 없습니다."));

        account.withdraw();
        oAuthAccountRepository.flush();
        memberDataDeletionService.deleteAll(memberId);
        memberRepository.delete(member);
    }

    private OAuthLoginResponse loginExisting(OAuthAccount account, String displayName) {
        if (account.isWithdrawn() || !account.getMember().isActive()) {
            return rejoin(account, displayName);
        }
        Member member = account.getMember();
        member.login(displayName);
        return loginResponse(member, account);
    }

    private OAuthLoginResponse rejoin(OAuthAccount account, String displayName) {
        Member previousMember = account.getMember();
        Member newMember = memberRepository.save(Member.createUser(displayName));
        account.reactivate(newMember);
        oAuthAccountRepository.flush();

        if (previousMember != null) {
            Long previousMemberId = previousMember.getId();
            memberDataDeletionService.deleteAll(previousMemberId);
            memberRepository.delete(previousMember);
        }
        return loginResponse(newMember, account);
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
