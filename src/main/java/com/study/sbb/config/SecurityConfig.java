package com.study.sbb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // 내부적으로 SpringSecurityFilterChain이 동작하여 URL 필터가 적용
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 애너테이션이 동작할 수 있도록
public class SecurityConfig {
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests()
		.requestMatchers(new AntPathRequestMatcher("/**"))
		.permitAll() // 로그인을 하지 않더라도 모든 페이지에 접근
		
		.and()
        .csrf().ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")) // 스프링 시큐리티가 CSRF 처리시 H2 콘솔은 예외로 처리할 수 있도록
        
        .and()
        .headers()
        .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
		// X-Frame-Options 헤더값을 사용하여 사이트의 콘텐츠가 다른 사이트에 포함되는 것을 방지. 이를 해결하기 위한 코드
        
        .and()
        .formLogin()
        .loginPage("/user/login")
        .defaultSuccessUrl("/")
        
        .and()
        .logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
        .logoutSuccessUrl("/")
        .invalidateHttpSession(true)
    ;
		return http.build();
	}

	
	@Bean
    PasswordEncoder passwordEncoder() { // PasswordEncoder 빈(bean)을 만드는 가장 쉬운 방법
        return new BCryptPasswordEncoder();
    }
	
	@Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception { 
        return authenticationConfiguration.getAuthenticationManager(); // AuthenticationManager는 스프링 시큐리티의 인증을 담당
        // UserSecurityService와 PasswordEncoder가 자동으로 설정
    }
	
}