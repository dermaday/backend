package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.dto.response.MemberAuthenticationResponse;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberAuthenticationServiceTest {

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final MemberAuthenticationService service = new MemberAuthenticationService(memberRepository);

    @Test
    void returnsOnlyActiveMemberData() {
        Member activeMember = mock(Member.class);
        when(activeMember.isActive()).thenReturn(true);
        when(activeMember.getId()).thenReturn(1L);
        when(activeMember.getDisplayName()).thenReturn("사용자");
        when(activeMember.getRole()).thenReturn(MemberRole.USER);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));

        MemberAuthenticationResponse result = service.findActiveMember(1L).orElseThrow();

        assertEquals(1L, result.memberId());
        assertEquals("사용자", result.displayName());
        assertEquals(MemberRole.USER, result.role());
    }

    @Test
    void excludesWithdrawnMember() {
        Member withdrawnMember = mock(Member.class);
        when(withdrawnMember.isActive()).thenReturn(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(withdrawnMember));

        assertTrue(service.findActiveMember(1L).isEmpty());
    }
}
