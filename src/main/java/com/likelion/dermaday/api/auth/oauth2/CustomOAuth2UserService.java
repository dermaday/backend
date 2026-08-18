package com.likelion.dermaday.api.auth.oauth2;

import com.likelion.dermaday.api.member.dto.request.OAuthLoginRequest;
import com.likelion.dermaday.api.member.dto.response.OAuthLoginResponse;
import com.likelion.dermaday.api.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2ProfileMapper profileMapper;
    private final MemberService memberService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProfile profile = profileMapper.map(registrationId, oauth2User.getAttributes());
        OAuthLoginResponse login = memberService.loginOrCreate(new OAuthLoginRequest(
                profile.provider(),
                profile.providerUserId(),
                profile.displayName()
        ));

        return new OAuth2MemberPrincipal(
                login.memberId(),
                login.displayName(),
                login.role(),
                login.provider(),
                oauth2User.getAttributes(),
                List.of(new SimpleGrantedAuthority(login.role().authority()))
        );
    }
}
