package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.OAuthAccount;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.dto.request.OAuthLoginRequest;
import com.likelion.dermaday.api.member.dto.response.OAuthLoginResponse;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.member.repository.OAuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OAuthAccountRepository oAuthAccountRepository;

    @Mock
    private MemberDataDeletionService memberDataDeletionService;

    @InjectMocks
    private MemberService memberService;

    @Test
    void createsMemberForNewOAuthAccount() {
        OAuthLoginRequest request = new OAuthLoginRequest(OAuthProvider.KAKAO, "kakao-id", "카카오 사용자");
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "kakao-id"))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(oAuthAccountRepository.save(any(OAuthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthLoginResponse result = memberService.loginOrCreate(request);

        assertEquals(OAuthProvider.KAKAO, result.provider());
        assertEquals("카카오 사용자", result.displayName());
    }

    @Test
    void createsFreshMemberAndReconnectsWithdrawnOAuthAccount() {
        Member member = Member.createUser("이전 이름");
        member.withdraw();
        OAuthAccount existing = OAuthAccount.create(member, OAuthProvider.NAVER, "naver-id");
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.NAVER, "naver-id"))
                .thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuthLoginResponse result = memberService.loginOrCreate(
                new OAuthLoginRequest(OAuthProvider.NAVER, "naver-id", "최신 이름")
        );

        assertTrue(existing.getMember().isActive());
        assertEquals("최신 이름", existing.getMember().getDisplayName());
        assertEquals("최신 이름", result.displayName());
        assertEquals(OAuthProvider.NAVER, result.provider());
    }

    @Test
    void updatesExistingActiveMemberWithoutCreatingAnotherMember() {
        Member member = Member.createUser("기존 이름");
        OAuthAccount existing = OAuthAccount.create(member, OAuthProvider.KAKAO, "kakao-id");
        when(oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "kakao-id"))
                .thenReturn(Optional.of(existing));

        OAuthLoginResponse result = memberService.loginOrCreate(
                new OAuthLoginRequest(OAuthProvider.KAKAO, "kakao-id", "변경된 이름")
        );

        assertEquals("변경된 이름", member.getDisplayName());
        assertEquals("변경된 이름", result.displayName());
        verify(memberRepository, never()).save(any(Member.class));
    }
}
