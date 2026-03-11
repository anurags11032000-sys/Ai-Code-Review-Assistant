package com.aicodereviewassistant.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return Map.of("authenticated", false);
        }

        String email = stringAttr(oAuth2User, "email");
        String name = stringAttr(oAuth2User, "name");
        String picture = stringAttr(oAuth2User, "picture");

        return Map.of(
                "authenticated", true,
                "email", email == null ? "" : email,
                "name", name == null ? "" : name,
                "picture", picture == null ? "" : picture
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    private String stringAttr(OAuth2User user, String key) {
        Object value = user.getAttributes().get(key);
        return value == null ? null : value.toString();
    }
}
