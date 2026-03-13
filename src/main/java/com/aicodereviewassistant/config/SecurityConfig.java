package com.aicodereviewassistant.config;

import com.aicodereviewassistant.security.ApiKeyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${frontend.url:}")
    private String frontendUrl;

    @Value("${frontend.urls:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String frontendUrls;

    @Value("${frontend.origin-patterns:https://*.up.railway.app,http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String frontendOriginPatterns;

    @Value("${frontend.auth-success-url:http://localhost:5173}")
    private String frontendAuthSuccessUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/api/v1/health").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl(frontendAuthSuccessUrl, true))
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String[] origins = StringUtils.commaDelimitedListToStringArray(frontendUrls);
        String[] originPatterns = StringUtils.commaDelimitedListToStringArray(frontendOriginPatterns);
        if (StringUtils.hasText(frontendUrl)) {
            String merged = frontendUrls + "," + frontendUrl;
            origins = StringUtils.commaDelimitedListToStringArray(merged);
            String mergedPatterns = frontendOriginPatterns + "," + frontendUrl;
            originPatterns = StringUtils.commaDelimitedListToStringArray(mergedPatterns);
        }

        configuration.setAllowedOrigins(Arrays.asList(origins));
        configuration.setAllowedOriginPatterns(Arrays.asList(originPatterns));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
