package com.spliteasy.expense_sharing.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.spliteasy.expense_sharing.entity.User;
import com.spliteasy.expense_sharing.repository.UserRepository;
import com.spliteasy.expense_sharing.util.JwtUtil;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import model.Role;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, java.io.IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");

		// Save user if not exists
		User user = userRepository.findByEmail(email).orElseGet(() -> {
			User u = new User();
			u.setEmail(email);
			u.setName(name);
			u.setRole(Role.USER);
			return userRepository.save(u);
		});

		// Generate JWT
		String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

		// Send token in response header (or body as JSON)
		//response.sendRedirect("/users?"+token);
		response.setHeader("Authorization", "Bearer " + token);
	}

}
