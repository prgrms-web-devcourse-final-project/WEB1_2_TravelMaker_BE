package edu.example.wayfarer.config;

import edu.example.wayfarer.auth.constant.SecurityConstants;
import edu.example.wayfarer.auth.filter.*;
import edu.example.wayfarer.auth.userdetails.PrincipalDetailsService;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.service.AuthService; // AuthService 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CustomDaoAuthenticationProvider customDaoAuthenticationProvider() {
        CustomDaoAuthenticationProvider provider = new CustomDaoAuthenticationProvider();
        provider.setUserDetailsService(principalDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception { // AuthService 추가

        // cors disable
        http.cors(cors -> cors
                .configurationSource(CorsConfig.apiConfigurationSource()));

        // csrf disable
        http.csrf(AbstractHttpConfigurer::disable);

        // form 로그인 방식 disable
        http.formLogin(AbstractHttpConfigurer::disable);

        // http basic 인증 방식 disable
        http.httpBasic(AbstractHttpConfigurer::disable);

        // Session Stateless하게 관리
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 경로별 인가
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                .requestMatchers(SecurityConstants.allowedUrls).permitAll()
                .requestMatchers("/auth/google/**").permitAll() // 구글 소셜 로그인 경로 추가
                .requestMatchers("/login/oauth2/code/google").permitAll() // OAuth 리다이렉트 경로 허용
                .anyRequest().authenticated()
        );

        http.exceptionHandling(
                (configurer ->
                        configurer
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
        );

        // JwtFilter를 UsernamePasswordAuthenticationFilter 이전에 추가
        http.addFilterBefore(new JwtFilter(jwtUtil, principalDetailsService, authService),
                UsernamePasswordAuthenticationFilter.class);

        // JwtExceptionFilter를 JwtFilter 이후에 추가
        http.addFilterAfter(new JwtExceptionFilter(), JwtFilter.class);

        // LoginFilter를 UsernamePasswordAuthenticationFilter 위치에 추가
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
