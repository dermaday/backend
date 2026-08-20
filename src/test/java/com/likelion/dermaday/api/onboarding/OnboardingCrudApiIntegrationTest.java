package com.likelion.dermaday.api.onboarding;

import com.likelion.dermaday.api.auth.principal.MemberJwtAuthenticationToken;
import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.cosmetic.domain.CosmeticType;
import com.likelion.dermaday.api.cosmetic.domain.IngredientType;
import com.likelion.dermaday.api.cosmetic.dto.request.CreateCosmeticRequest;
import com.likelion.dermaday.api.cosmetic.dto.response.CosmeticResponse;
import com.likelion.dermaday.api.cosmetic.service.CosmeticService;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.domain.MemberRole;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.treatment.domain.TreatmentReaction;
import com.likelion.dermaday.api.treatment.domain.TreatmentType;
import com.likelion.dermaday.api.treatment.dto.request.CreateTreatmentRequest;
import com.likelion.dermaday.api.treatment.dto.request.TreatmentItemRequest;
import com.likelion.dermaday.api.treatment.dto.response.TreatmentResponse;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OnboardingCrudApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private CosmeticService cosmeticService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.createUser("API CRUD 회원"));
    }

    @Test
    void completesSkinProfileCrud() throws Exception {
        mockMvc.perform(put("/api/v1/skin-profile")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skinType\":\"COMBINATION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skinType").value("COMBINATION"))
                .andExpect(jsonPath("$.data.name").value("복합성"));

        mockMvc.perform(get("/api/v1/skin-profile")
                        .with(authentication(authenticationOf(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skinType").value("COMBINATION"));

        mockMvc.perform(delete("/api/v1/skin-profile")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/skin-profile")
                        .with(authentication(authenticationOf(member))))
                .andExpect(status().isNotFound());
    }

    @Test
    void completesCosmeticCrudWithinTreatmentRecord() throws Exception {
        TreatmentResponse treatment = createTreatment(member);

        MvcResult createResult = mockMvc.perform(post("/api/v1/cosmetics")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "treatmentRecordId": %d,
                                  "name": "센텔라 스킨",
                                  "productType": "TONER_SKIN",
                                  "ingredients": ["RETINOL", "AHA"],
                                  "imageObjectKey": null
                                }
                                """.formatted(treatment.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.treatmentRecordId").value(treatment.id()))
                .andExpect(jsonPath("$.data.ingredients.length()").value(2))
                .andReturn();
        long cosmeticId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/v1/cosmetics")
                        .param("treatmentRecordId", treatment.id().toString())
                        .with(authentication(authenticationOf(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(cosmeticId));

        mockMvc.perform(put("/api/v1/cosmetics/{cosmeticId}", cosmeticId)
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "비타민 크림",
                                  "productType": "LOTION_CREAM",
                                  "ingredients": ["VITAMIN_C"],
                                  "imageObjectKey": "assets/cosmetics/vitamin-cream.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("비타민 크림"))
                .andExpect(jsonPath("$.data.productType").value("LOTION_CREAM"))
                .andExpect(jsonPath("$.data.ingredients[0]").value("VITAMIN_C"));

        mockMvc.perform(delete("/api/v1/cosmetics/{cosmeticId}", cosmeticId)
                        .with(authentication(authenticationOf(member)))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/cosmetics/{cosmeticId}", cosmeticId)
                        .with(authentication(authenticationOf(member))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsMalformedAndInvalidRequests() throws Exception {
        TreatmentResponse treatment = createTreatment(member);

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{
                                    "treatmentType": "ULTHERA",
                                    "treatedOn": "%s",
                                    "reaction": "COMFORTABLE"
                                  }]
                                }
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{
                                    "treatmentType": "NOT_A_TREATMENT",
                                    "treatedOn": "2026-08-01",
                                    "reaction": "COMFORTABLE"
                                  }]
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/treatments")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "treatmentType": "ULTHERA",
                                      "treatedOn": "2026-08-01",
                                      "reaction": "COMFORTABLE"
                                    },
                                    {
                                      "treatmentType": "ULTHERA",
                                      "treatedOn": "2026-08-02",
                                      "reaction": "IRRITATED"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/cosmetics")
                        .with(authentication(authenticationOf(member)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "treatmentRecordId": %d,
                                  "name": " ",
                                  "productType": "OIL",
                                  "ingredients": []
                                }
                                """.formatted(treatment.id())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/cosmetics")
                        .with(authentication(authenticationOf(member))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hidesOtherMembersResourcesWithNotFound() throws Exception {
        TreatmentResponse treatment = createTreatment(member);
        CosmeticResponse cosmetic = cosmeticService.create(member.getId(), new CreateCosmeticRequest(
                treatment.id(),
                "소유자 제품",
                CosmeticType.OIL,
                Set.of(IngredientType.BHA),
                null
        ));
        Member other = memberRepository.save(Member.createUser("공격자"));

        mockMvc.perform(get("/api/v1/treatments/{recordId}", treatment.id())
                        .with(authentication(authenticationOf(other))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/cosmetics/{cosmeticId}", cosmetic.id())
                        .with(authentication(authenticationOf(other))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/cosmetics")
                        .param("treatmentRecordId", treatment.id().toString())
                        .with(authentication(authenticationOf(other))))
                .andExpect(status().isNotFound());
    }

    private TreatmentResponse createTreatment(Member owner) {
        return treatmentService.create(owner.getId(), new CreateTreatmentRequest(List.of(
                new TreatmentItemRequest(
                        TreatmentType.ULTHERA,
                        LocalDate.of(2026, 8, 8),
                        TreatmentReaction.COMFORTABLE
                )
        )));
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
