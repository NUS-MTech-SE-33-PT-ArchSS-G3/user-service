package com.biddergod.user_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CognitoConfig cognitoConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/api/health", "/api/info").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // OpenAPI/Swagger endpoints
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Public reputation endpoints (read-only)
                .requestMatchers(HttpMethod.GET, "/api/reputation/top-users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reputation/trusted-users").permitAll()

                // Public feedback endpoints (read-only)
                .requestMatchers(HttpMethod.GET, "/api/feedback/user/**").permitAll()

                // Protected endpoints - require authentication
                .requestMatchers(HttpMethod.POST, "/api/feedback").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/reputation/user/*/recalculate").authenticated()
                .requestMatchers("/api/reputation/recalculate-all").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            )
            .headers(headers -> headers.frameOptions().sameOrigin()); // For H2 console

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // For testing without Cognito connection, use a mock decoder
        // In production, uncomment the lines below:
         String issuerUri = cognitoConfig.getIssuerUri();
         return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        // Mock JWT decoder for testing
//        return token -> {
//            throw new RuntimeException("JWT validation disabled for testing");
//        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}