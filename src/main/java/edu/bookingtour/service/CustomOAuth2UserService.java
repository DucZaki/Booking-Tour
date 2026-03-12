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
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private NguoiDungService nguoiDungService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {
        OAuth2User oauthUser;
        try {
            oauthUser = super.loadUser(request);
        } catch (OAuth2AuthenticationException e) {
            System.err.println("OAuth2 error: " + e.getError().getErrorCode() + " - " + e.getMessage());
            throw e;
        }

        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String provider = registrationId.toUpperCase();

        // Identifiers: Google uses "sub", Facebook uses "id"
        String providerId = (String) attributes.get("google".equals(registrationId) ? "sub" : "id");

        // Determine unique username for lookup
        String tenDangNhap = (email != null) ? email : registrationId + "_" + providerId;

        // Save or update user in database
        nguoiDungService.findByTenDangNhap(tenDangNhap).orElseGet(() -> {
            NguoiDung nd = new NguoiDung();
            nd.setTenDangNhap(tenDangNhap);
            nd.setEmail(email);
            nd.setHoTen(name);
            nd.setVaiTro("USER");
            nd.setProvider(provider);
            return nguoiDungService.save(nd);
        });

        // Add the unique username to attributes so we can use it as the name attribute
        // key
        attributes.put("userNameKey", tenDangNhap);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "userNameKey");
    }
}
