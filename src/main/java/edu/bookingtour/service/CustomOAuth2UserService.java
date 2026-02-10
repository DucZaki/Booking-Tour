package edu.bookingtour.service;

import edu.bookingtour.entity.NguoiDung;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private NguoiDungService nguoiDungService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();

        String email = null;
        String name = null;
        String provider = null;

        if ("google".equals(registrationId)) {
            email = oauthUser.getAttribute("email");
            name = oauthUser.getAttribute("name");
            provider = "GOOGLE";
        }

        if ("facebook".equals(registrationId)) {
            email = oauthUser.getAttribute("email");
            name = oauthUser.getAttribute("name");
            provider = "FACEBOOK";
        }

        if (email != null) {
            String finalProvider = provider;
            String finalName = name;
            String finalEmail = email;
            nguoiDungService.findByEmail(email).orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail(finalEmail);
                nd.setTenDangNhap(finalEmail);
                nd.setHoTen(finalName);
                nd.setVaiTro("USER");
                nd.setProvider(finalProvider);
                return nguoiDungService.save(nd);
            });
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oauthUser.getAttributes(),
                "email"
        );
    }


}
