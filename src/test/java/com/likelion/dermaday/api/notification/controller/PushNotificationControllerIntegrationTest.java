package com.likelion.dermaday.api.notification.controller;

import com.likelion.dermaday.api.auth.principal.MemberJwtAuthenticationToken;
import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.notification.repository.PushInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PushNotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PushInstallationRepository installationRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.createUser("푸시 API 회원"));
    }

    @Test
    void requiresAuthenticationAndCsrfForPushSettings() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/push/preference"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/notifications/push/preference")
                        .with(authentication(authenticationOf(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/notifications/push/preference")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void validatesFidAndKeepsInstallationsIsolatedByMember() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/push/installations")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fid\":\" \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/notifications/push/installations")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fid\":\"owner-fid\"}"))
                .andExpect(status().isOk());

        Member other = memberRepository.save(Member.createUser("다른 푸시 API 회원"));
        mockMvc.perform(delete("/api/v1/notifications/push/installations")
                        .with(authentication(authenticationOf(other)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fid\":\"owner-fid\"}"))
                .andExpect(status().isOk());

        Long ownerId = installationRepository.findByFid("owner-fid")
                .orElseThrow()
                .getMember()
                .getId();
        assertEquals(member.getId(), ownerId);
    }

    private MemberJwtAuthenticationToken authenticationOf(Member authenticatedMember) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(authenticatedMember.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        MemberPrincipal principal = new MemberPrincipal(
                authenticatedMember.getId(),
                authenticatedMember.getDisplayName(),
                MemberRole.USER,
                OAuthProvider.KAKAO
        );
        return new MemberJwtAuthenticationToken(
                jwt,
                principal,
                List.of(new SimpleGrantedAuthority(MemberRole.USER.authority()))
        );
    }
}
