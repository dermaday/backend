package com.likelion.dermaday.api.member.service;

import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.OAuthAccount;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.dto.request.OAuthLoginRequest;
import com.likelion.dermaday.api.member.dto.response.OAuthLoginResponse;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.member.repository.OAuthAccountRepository;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.skin.repository.SkinProfileRepository;
import com.likelion.dermaday.api.skin.service.SkinProfileService;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
import com.likelion.dermaday.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberLifecycleIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OAuthAccountRepository oAuthAccountRepository;

    @Autowired
    private SkinProfileService skinProfileService;

    @Autowired
    private SkinProfileRepository skinProfileRepository;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private TreatmentRecordRepository treatmentRecordRepository;

    @Autowired
    private CosmeticService cosmeticService;

    @Autowired
    private CosmeticRepository cosmeticRepository;

    @Test
    void hardDeletesMemberAndReconnectsProviderAccountOnRejoin() {
        OAuthLoginRequest request = new OAuthLoginRequest(OAuthProvider.KAKAO, "provider-user", "첫 회원");
        OAuthLoginResponse firstLogin = memberService.loginOrCreate(request);
        skinProfileService.upsert(firstLogin.memberId(), SkinType.NORMAL);
        TreatmentResponse treatment = treatmentService.create(firstLogin.memberId(), new CreateTreatmentRequest(
                List.of(new TreatmentItemRequest(
                        TreatmentType.SQUARE_JAW_BODY_BOTOX,
                        LocalDate.of(2026, 8, 8),
                        TreatmentReaction.COMFORTABLE
                ))
        ));
        CosmeticResponse cosmetic = cosmeticService.create(firstLogin.memberId(), new CreateCosmeticRequest(
                treatment.id(),
                "센텔라 스킨",
                CosmeticType.TONER_SKIN,
                Set.of(IngredientType.RETINOL),
                "assets/cosmetics/centella-skin.png"
        ));

        memberService.withdraw(firstLogin.memberId());
        memberRepository.flush();

        OAuthAccount withdrawnAccount = oAuthAccountRepository
                .findByProviderAndProviderUserId(OAuthProvider.KAKAO, "provider-user")
                .orElseThrow();
        assertFalse(memberRepository.existsById(firstLogin.memberId()));
        assertFalse(skinProfileRepository.findByMember_Id(firstLogin.memberId()).isPresent());
        assertFalse(treatmentRecordRepository.existsById(treatment.id()));
        assertFalse(cosmeticRepository.existsById(cosmetic.id()));
        assertNull(withdrawnAccount.getMember());
        assertTrue(withdrawnAccount.isWithdrawn());

        OAuthLoginResponse rejoined = memberService.loginOrCreate(
                new OAuthLoginRequest(OAuthProvider.KAKAO, "provider-user", "재가입 회원")
        );

        assertNotEquals(firstLogin.memberId(), rejoined.memberId());
        assertTrue(memberRepository.existsById(rejoined.memberId()));
        assertFalse(withdrawnAccount.isWithdrawn());
    }

    @Test
    void preservesMemberWhenOAuthAccountIsMissing() {
        Member member = memberRepository.save(Member.createUser("OAuth 없는 회원"));

        assertThrows(NotFoundException.class, () -> memberService.withdraw(member.getId()));

        assertTrue(memberRepository.existsById(member.getId()));
    }

    @Test
    void treatsSameProviderUserIdFromDifferentProvidersAsDifferentAccounts() {
        OAuthLoginResponse kakao = memberService.loginOrCreate(
                new OAuthLoginRequest(OAuthProvider.KAKAO, "same-id", "카카오 회원")
        );
        OAuthLoginResponse naver = memberService.loginOrCreate(
                new OAuthLoginRequest(OAuthProvider.NAVER, "same-id", "네이버 회원")
        );

        assertNotEquals(kakao.memberId(), naver.memberId());
        assertTrue(memberRepository.existsById(kakao.memberId()));
        assertTrue(memberRepository.existsById(naver.memberId()));
    }
}
