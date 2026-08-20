package com.likelion.dermaday.api.skin.domain;

import com.likelion.dermaday.api.member.domain.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkinProfileTest {

    @Test
    void createsAndChangesCurrentSkinType() {
        SkinProfile profile = SkinProfile.create(Member.createUser("회원"), SkinType.DRY);

        profile.change(SkinType.COMBINATION);

        assertEquals(SkinType.COMBINATION, profile.getSkinType());
    }

    @Test
    void rejectsMissingMemberOrSkinType() {
        Member member = Member.createUser("회원");

        assertThrows(NullPointerException.class, () -> SkinProfile.create(null, SkinType.NORMAL));
        assertThrows(NullPointerException.class, () -> SkinProfile.create(member, null));
        assertThrows(NullPointerException.class, () -> SkinProfile.create(member, SkinType.NORMAL).change(null));
    }
}
