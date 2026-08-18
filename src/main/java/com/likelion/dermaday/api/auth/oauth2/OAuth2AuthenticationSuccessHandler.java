package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.auth.cookie.AuthCookieService;
import com.likelion.dermaday.api.auth.jwt.JwtTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;
    private final OAuth2RedirectUriFactory redirectUriFactory;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OAuth2MemberPrincipal principal)) {
            throw new ServletException("Unexpected OAuth2 principal type");
        }

        String accessToken = jwtTokenService.createAccessToken(
                principal.memberId(),
                principal.role(),
                principal.provider()
        );
        authCookieService.addAccessToken(response, accessToken);
        invalidateSession(request);
        redirectStrategy.sendRedirect(request, response, redirectUriFactory.success(principal.provider()));
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
