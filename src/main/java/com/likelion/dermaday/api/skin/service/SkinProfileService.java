package com.likelion.dermaday.api.skin.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.skin.domain.SkinProfile;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.skin.dto.response.SkinProfileResponse;
import com.likelion.dermaday.api.skin.repository.SkinProfileRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import com.likelion.dermaday.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkinProfileService {

    private final SkinProfileRepository skinProfileRepository;
    private final MemberRepository memberRepository;

    public SkinProfileResponse find(Long memberId) {
        return skinProfileRepository.findByMember_Id(memberId)
                .map(SkinProfileResponse::from)
                .orElseThrow(() -> new NotFoundException("등록된 피부 타입이 없습니다."));
    }

    @Transactional
    public SkinProfileResponse upsert(Long memberId, SkinType skinType) {
        SkinProfile profile = skinProfileRepository.findByMember_Id(memberId)
                .map(existing -> {
                    existing.change(skinType);
                    return existing;
                })
                .orElseGet(() -> create(memberId, skinType));
        return SkinProfileResponse.from(skinProfileRepository.save(profile));
    }

    @Transactional
    public void delete(Long memberId) {
        SkinProfile profile = skinProfileRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new NotFoundException("등록된 피부 타입이 없습니다."));
        skinProfileRepository.delete(profile);
    }

    private SkinProfile create(Long memberId, SkinType skinType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER.getMessage()));
        return SkinProfile.create(member, skinType);
    }
}
