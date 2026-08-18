package com.likelion.dermaday.api.member.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
