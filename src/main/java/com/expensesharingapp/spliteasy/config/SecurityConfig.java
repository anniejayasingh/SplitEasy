package com.expensesharingapp.spliteasy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/users/**","/groups/**","/expenses/**","/settlements/**"))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/users/**").permitAll()
                .requestMatchers("/groups/**").permitAll()
                .requestMatchers("/expenses/**").permitAll()
                .requestMatchers("/settlements/**").permitAll()
                .anyRequest().authenticated()
        );


        return http.build();
    }
}
