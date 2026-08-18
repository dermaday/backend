package com.likelion.dermaday.api.member.controller;

import com.likelion.dermaday.api.auth.cookie.AuthCookieService;
import com.likelion.dermaday.api.auth.principal.MemberPrincipal;
import com.likelion.dermaday.api.member.dto.response.MemberMeResponse;
import com.likelion.dermaday.common.response.ApiResponse;
import com.likelion.dermaday.common.response.SuccessStatus;
import com.likelion.dermaday.api.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthCookieService authCookieService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberMeResponse>> me(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        MemberMeResponse data = new MemberMeResponse(
                principal.id(),
                principal.displayName(),
                principal.role(),
                principal.provider()
        );
        return ApiResponse.success(SuccessStatus.MEMBER_INFO_GET_SUCCESS, data);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletResponse response
    ) {
        memberService.withdraw(principal.id());
        authCookieService.clearAccessToken(response);
        return ApiResponse.successOnly(SuccessStatus.MEMBER_RESIGN_DELETE_SUCCESS);
    }
}
