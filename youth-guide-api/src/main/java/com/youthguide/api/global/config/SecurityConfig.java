// global/config/SecurityConfig.java
package com.youthguide.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/dev/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/certificates/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}