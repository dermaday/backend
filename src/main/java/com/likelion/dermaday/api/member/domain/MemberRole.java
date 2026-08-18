package com.likelion.dermaday.api.member.domain;

public enum MemberRole {
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
