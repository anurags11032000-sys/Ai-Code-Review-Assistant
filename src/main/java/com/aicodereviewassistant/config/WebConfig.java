package com.aicodereviewassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url:}")
    private String frontendUrl;

    @Value("${frontend.urls:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String frontendUrls;

    @Value("${frontend.origin-patterns:https://*.up.railway.app,http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String frontendOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = StringUtils.commaDelimitedListToStringArray(frontendUrls);
        String[] originPatterns = StringUtils.commaDelimitedListToStringArray(frontendOriginPatterns);
        if (StringUtils.hasText(frontendUrl)) {
            String merged = frontendUrls + "," + frontendUrl;
            origins = StringUtils.commaDelimitedListToStringArray(merged);
            String mergedPatterns = frontendOriginPatterns + "," + frontendUrl;
            originPatterns = StringUtils.commaDelimitedListToStringArray(mergedPatterns);
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedOriginPatterns(originPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("*");
    }
}
