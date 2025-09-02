package com.shop.tradezone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shop.tradezone.service.MemberSecurityService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// URL 별 권한 설정
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/member/join", "/member/check-username", "/member/login",
								"/member/updateidentity", "/member/resetpassword", "/**", "/notice/**", "/css/**",
								"/js/**", "/images/**", "/api/items/main", "/callback", "/member/passwordcode", "/**")
						.permitAll().requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/member/**", "/register/**", "/chat/**", "/callback", "/items/**",
								"/reviews/**")
						.hasAnyRole("USER", "ADMIN").anyRequest().authenticated())
				// 로그인 설정
				.formLogin(formLogin -> formLogin.loginPage("/member/login").loginProcessingUrl("/member/login")
						.usernameParameter("email").defaultSuccessUrl("/").permitAll())

				// 로그아웃 설정
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true));
		http.csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())
				.ignoringRequestMatchers("/member/updateidentity", "/member/resetpassword", "/member/passwordcode"));

		return http.build();
	}

	// 비밀번호 암호화
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(MemberSecurityService memberSecurityService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(memberSecurityService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}
}