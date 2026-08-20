package com.likelion.dermaday.api.notification.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.domain.PushInstallation;
import com.likelion.dermaday.api.notification.dto.response.PushPreferenceResponse;
import com.likelion.dermaday.api.notification.repository.PushInstallationRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import com.likelion.dermaday.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushSubscriptionService {

    private final MemberRepository memberRepository;
    private final PushInstallationRepository pushInstallationRepository;

    public PushPreferenceResponse getPreference(Long memberId) {
        return new PushPreferenceResponse(getMember(memberId).isPushEnabled());
    }

    @Transactional
    public PushPreferenceResponse updatePreference(Long memberId, boolean enabled) {
        Member member = getMember(memberId);
        member.changePushEnabled(enabled);
        return new PushPreferenceResponse(member.isPushEnabled());
    }

    @Transactional
    public void register(Long memberId, String fid) {
        Member member = getMember(memberId);
        String normalizedFid = fid.trim();
        PushInstallation installation = pushInstallationRepository.findByFid(normalizedFid)
                .map(existing -> {
                    existing.assignTo(member);
                    return existing;
                })
                .orElseGet(() -> PushInstallation.create(member, normalizedFid));
        pushInstallationRepository.save(installation);
    }

    @Transactional
    public void remove(Long memberId, String fid) {
        pushInstallationRepository.deleteByMember_IdAndFid(memberId, fid.trim());
    }

    public List<String> findFids(Long memberId) {
        return pushInstallationRepository.findAllByMember_IdOrderByCreatedAtAsc(memberId).stream()
                .map(PushInstallation::getFid)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeInvalidFids(List<String> invalidFids) {
        if (!invalidFids.isEmpty()) {
            pushInstallationRepository.deleteAllByFidIn(invalidFids);
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER.getMessage()));
    }
}
