package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.auth.cookie.AuthCookieService;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String GENERIC_ERROR = "oauth2_login_failed";
    private static final Set<String> EXPOSED_ERROR_CODES = Set.of(
            "access_denied",
            "missing_required_profile"
    );

    private final AuthCookieService authCookieService;
    private final OAuth2RedirectUriFactory redirectUriFactory;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        OAuthProvider provider = providerFromRequest(request);
        String errorCode = exposedErrorCode(exception);
        log.warn("OAuth2 login failed. provider={}, errorCode={}", provider, errorCode, exception);

        authCookieService.clearAccessToken(response);
        invalidateSession(request);
        redirectStrategy.sendRedirect(request, response, redirectUriFactory.failure(provider, errorCode));
    }

    private String exposedErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();
            if (EXPOSED_ERROR_CODES.contains(errorCode)) {
                return errorCode;
            }
        }
        return GENERIC_ERROR;
    }

    private OAuthProvider providerFromRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        for (OAuthProvider provider : OAuthProvider.values()) {
            if (requestUri.endsWith("/" + provider.registrationId())) {
                return provider;
            }
        }
        return OAuthProvider.KAKAO;
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
