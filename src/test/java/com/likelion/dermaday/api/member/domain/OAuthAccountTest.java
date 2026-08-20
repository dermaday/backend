package com.likelion.dermaday.api.member.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthAccountTest {

    @Test
    void rejectsInvalidRequiredValues() {
        Member member = Member.createUser("사용자");

        assertThrows(IllegalArgumentException.class, () -> OAuthAccount.create(null, OAuthProvider.KAKAO, "id"));
        assertThrows(IllegalArgumentException.class, () -> OAuthAccount.create(member, null, "id"));
        assertThrows(IllegalArgumentException.class, () -> OAuthAccount.create(member, OAuthProvider.KAKAO, " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> OAuthAccount.create(member, OAuthProvider.KAKAO, "a".repeat(256))
        );
    }

    @Test
    void tombstonesAndReconnectsAccountToFreshMember() {
        Member previous = Member.createUser("이전 회원");
        OAuthAccount account = OAuthAccount.create(previous, OAuthProvider.KAKAO, "provider-id");

        account.withdraw();

        assertTrue(account.isWithdrawn());
        assertNull(account.getMember());
        assertTrue(account.getDeletedAt() != null);

        Member rejoined = Member.createUser("재가입 회원");
        account.reactivate(rejoined);

        assertFalse(account.isWithdrawn());
        assertSame(rejoined, account.getMember());
        assertNull(account.getDeletedAt());
    }
}
