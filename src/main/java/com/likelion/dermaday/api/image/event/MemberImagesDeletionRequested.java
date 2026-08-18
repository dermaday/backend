package com.likelion.dermaday.api.image.event;

import java.util.List;
import java.util.Objects;

public record MemberImagesDeletionRequested(
        Long memberId,
        List<String> objectKeys
) {
    public MemberImagesDeletionRequested {
        Objects.requireNonNull(memberId, "Member id must not be null");
        objectKeys = List.copyOf(Objects.requireNonNull(objectKeys, "Object keys must not be null"));
    }

    public static MemberImagesDeletionRequested of(Long memberId, String objectKey) {
        return new MemberImagesDeletionRequested(memberId, List.of(objectKey));
    }
}
