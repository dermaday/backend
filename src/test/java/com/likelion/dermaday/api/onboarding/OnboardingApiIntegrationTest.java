package com.likelion.dermaday.api.onboarding;

import com.likelion.dermaday.api.auth.principal.MemberJwtAuthenticationToken;
import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.repository.MemberRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OnboardingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void exposesOptionsAndCreatesTreatmentWithAuthenticatedMember() throws Exception {
        Member member = memberRepository.save(Member.createUser("API 회원"));

        mockMvc.perform(get("/api/v1/skin-profile/options")
                        .with(authentication(memberAuthentication(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[3].code").value("COMBINATION"))
                .andExpect(jsonPath("$.data[4].code").value("UNKNOWN"));

        mockMvc.perform(get("/api/v1/treatments/options")
                        .with(authentication(memberAuthentication(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].category").value("LIFTING_ELASTICITY"))
                .andExpect(jsonPath("$.data[0].treatments.length()").value(6))
                .andExpect(jsonPath("$.data[0].treatments[0].code").value("ULTHERA"))
                .andExpect(jsonPath("$.data[4].treatments.length()").value(5));

        mockMvc.perform(get("/api/v1/cosmetics/options")
                        .with(authentication(memberAuthentication(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productTypes.length()").value(4))
                .andExpect(jsonPath("$.data.ingredients.length()").value(5));

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(memberAuthentication(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "treatmentType": "ULTHERA",
                                      "treatedOn": "2026-08-08",
                                      "reaction": "COMFORTABLE"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].treatmentName").value("울쎄라"));
    }

    @Test
    void rejectsMutationWithoutCsrf() throws Exception {
        Member member = memberRepository.save(Member.createUser("CSRF 회원"));

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(memberAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsImageLargerThanTenMebibytes() throws Exception {
        Member member = memberRepository.save(Member.createUser("이미지 회원"));

        mockMvc.perform(post("/api/v1/images/presigned-upload")
                        .with(authentication(memberAuthentication(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contentType": "image/png",
                                  "fileSize": 10485761
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private MemberJwtAuthenticationToken memberAuthentication(Member member) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(member.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        MemberPrincipal principal = new MemberPrincipal(
                member.getId(),
                member.getDisplayName(),
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
