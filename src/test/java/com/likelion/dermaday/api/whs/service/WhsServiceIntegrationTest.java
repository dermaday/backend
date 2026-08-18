package com.likelion.dermaday.api.whs.service;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.api.member.repository.MemberRepository;
import com.likelion.dermaday.api.treatment.domain.TreatmentRecordSource;
import com.likelion.dermaday.api.treatment.repository.TreatmentRecordRepository;
import com.likelion.dermaday.api.whs.dto.response.WhsImportResponse;
import com.likelion.dermaday.api.whs.dto.response.WhsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
