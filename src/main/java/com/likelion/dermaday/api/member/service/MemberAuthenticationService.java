package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.member.dto.response.MemberAuthenticationResponse;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthenticationService {

    private final MemberRepository memberRepository;

    public Optional<MemberAuthenticationResponse> findActiveMember(Long memberId) {
        return memberRepository.findById(memberId)
                .filter(member -> member.isActive())
                .map(member -> new MemberAuthenticationResponse(
                        member.getId(),
                        member.getDisplayName(),
                        member.getRole()
                ));
    }
}
