package com.likelion.dermaday.api.member.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberTest {

    @Test
    void createsEveryNewMemberAsUser() {
        Member member = Member.createUser("사용자");

        assertEquals(MemberRole.USER, member.getRole());
        assertEquals(MemberStatus.ACTIVE, member.getStatus());
        assertTrue(member.isActive());
    }

    @Test
    void reactivatesWithdrawnMemberAndUpdatesDisplayName() {
        Member member = Member.createUser("이전 이름");
        member.withdraw();

        assertFalse(member.isActive());
        assertNotNull(member.getWithdrawnAt());

        member.login("새 이름");

        assertTrue(member.isActive());
        assertEquals("새 이름", member.getDisplayName());
        assertNull(member.getWithdrawnAt());
    }

    @Test
    void rejectsBlankOrTooLongDisplayName() {
        assertThrows(IllegalArgumentException.class, () -> Member.createUser(" "));
        assertThrows(IllegalArgumentException.class, () -> Member.createUser("a".repeat(101)));
    }
}
