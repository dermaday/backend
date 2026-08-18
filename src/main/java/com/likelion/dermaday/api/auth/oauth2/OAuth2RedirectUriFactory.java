package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.auth.config.AppProperties;
import com.likelion.dermaday.api.member.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2RedirectUriFactory {

    private final AppProperties properties;

    public String success(OAuthProvider provider) {
        return callback(provider).build().toUriString();
    }

    public String failure(OAuthProvider provider, String errorCode) {
        return callback(provider)
                .queryParam("error", errorCode)
                .build()
                .encode()
                .toUriString();
    }

    private UriComponentsBuilder callback(OAuthProvider provider) {
        return UriComponentsBuilder.fromUriString(properties.frontendBaseUrl())
                .pathSegment("auth", provider.registrationId(), "callback");
    }
}
