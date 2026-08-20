package com.likelion.dermaday.api.whs.service;

import com.likelion.dermaday.api.cosmetic.repository.CosmeticRepository;
import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.skin.domain.SkinType;
import com.likelion.dermaday.api.skin.service.SkinProfileService;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.api.treatment.service.TreatmentService;
import com.likelion.dermaday.api.whs.dto.response.WhsImportResponse;
import com.likelion.dermaday.api.whs.dto.response.WhsResponse;
import com.likelion.dermaday.api.whs.repository.WhsMockDataRepository;
import com.likelion.dermaday.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WhsServiceIntegrationTest {

    @Autowired
    private WhsService whsService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TreatmentRecordRepository treatmentRecordRepository;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private SkinProfileService skinProfileService;

    @Autowired
    private CosmeticRepository cosmeticRepository;

    @Autowired
    private WhsMockDataRepository whsMockDataRepository;

    @Test
    void previewsDatabaseMockAndImportsItOnlyOnce() {
        Member member = memberRepository.save(Member.createUser("염수빈"));

        WhsResponse preview = whsService.preview(member.getDisplayName());
        WhsImportResponse first = whsService.importData(member.getId());
        WhsImportResponse second = whsService.importData(member.getId());

        assertEquals("염수빈", preview.memberName());
        assertEquals(2, preview.treatments().size());
        assertEquals(2, preview.cosmetics().size());
        assertFalse(first.alreadyImported());
        assertTrue(second.alreadyImported());
        assertEquals(first.treatmentRecord().id(), second.treatmentRecord().id());
        assertEquals(
                1,
                treatmentRecordRepository.findAllByMember_IdOrderByCreatedAtDesc(member.getId()).stream()
                        .filter(record -> record.getSource() == TreatmentRecordSource.WHS_MOCK)
                        .count()
        );
    }

    @Test
    void importsMockAsOneRecordAndOverwritesCurrentSkinType() {
        Member member = memberRepository.save(Member.createUser("피부 변경 회원"));
        skinProfileService.upsert(member.getId(), SkinType.COMBINATION);

        WhsImportResponse imported = whsService.importData(member.getId());

        assertEquals(SkinType.NORMAL, imported.skinProfile().skinType());
        assertEquals(SkinType.NORMAL, skinProfileService.find(member.getId()).skinType());
        assertEquals(2, imported.treatmentRecord().items().size());
        assertEquals(2, imported.cosmetics().size());
        assertTrue(imported.cosmetics().stream().allMatch(cosmetic ->
                cosmetic.treatmentRecordId().equals(imported.treatmentRecord().id())
        ));
        assertEquals(
                2,
                cosmeticRepository.findAllByTreatmentRecord_IdAndTreatmentRecord_Member_IdOrderByCreatedAtAsc(
                        imported.treatmentRecord().id(),
                        member.getId()
                ).size()
        );
    }

    @Test
    void importsFreshMockAgainAfterPreviousWhsRecordIsDeleted() {
        Member member = memberRepository.save(Member.createUser("재가져오기 회원"));
        WhsImportResponse first = whsService.importData(member.getId());

        treatmentService.delete(member.getId(), first.treatmentRecord().id());
        WhsImportResponse reimported = whsService.importData(member.getId());

        assertFalse(reimported.alreadyImported());
        assertNotEquals(first.treatmentRecord().id(), reimported.treatmentRecord().id());
    }

    @Test
    void failsClearlyWhenDatabaseMockIsMissing() {
        whsMockDataRepository.deleteAll();
        whsMockDataRepository.flush();

        assertThrows(NotFoundException.class, () -> whsService.preview("회원"));
    }
}
