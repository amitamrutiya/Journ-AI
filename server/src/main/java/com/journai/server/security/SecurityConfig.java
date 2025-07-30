package com.journai.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ClerkAuthenticationFilter clerkAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/health", "/api/analyze-journal").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/webhooks/**", "/api/webhooks/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(clerkAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
