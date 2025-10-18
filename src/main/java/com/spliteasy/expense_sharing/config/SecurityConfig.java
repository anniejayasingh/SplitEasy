package com.spliteasy.expense_sharing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.spliteasy.expense_sharing.repository.UserRepository;
import com.spliteasy.expense_sharing.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	@Bean
	public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
		return new OAuth2LoginSuccessHandler(userRepository, jwtUtil);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	        .headers(headers -> headers.frameOptions().disable()) // allow H2 console frames
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/h2-console/**").permitAll()
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            .anyRequest().permitAll()
	        )
	        .oauth2Login(oauth2 -> oauth2
	            .successHandler(oAuth2LoginSuccessHandler())
	            .defaultSuccessUrl("/user", true) 
	        )
	        .oauth2ResourceServer(oauth2 -> oauth2
	            .jwt(jwt -> jwt.decoder(jwtDecoder()))
	        );

	    return http.build();
	}


	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(jwtUtil.getKey()).build();
	}
}
