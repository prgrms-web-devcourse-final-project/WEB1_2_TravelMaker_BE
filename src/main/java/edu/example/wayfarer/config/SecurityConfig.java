package edu.example.wayfarer.config;

import edu.example.wayfarer.auth.constant.SecurityConstants;
import edu.example.wayfarer.auth.filter.JwtAccessDeniedHandler;
import edu.example.wayfarer.auth.filter.JwtFilter;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.repository.TokenRepository;
import edu.example.wayfarer.service.AuthService;
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
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenRepository tokenRepository;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(cors -> cors
                .configurationSource(CorsConfig.apiConfigurationSource()));

        // CSRF 비활성화: REST API 서버에서는 세션 기반이 아닌 토큰 기반의 인증을 사용하므로 CSRF를 비활성화합니다.
        http.csrf(AbstractHttpConfigurer::disable);

        // 폼 로그인 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        // HTTP Basic 인증 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 세션 사용 비활성화 (Stateless)
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 경로별 인가 설정
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                .requestMatchers(SecurityConstants.allowedUrls).permitAll()
                .requestMatchers("/auth/google/**").permitAll() // 구글 소셜 로그인 경로 추가
                .requestMatchers("/login/oauth2/code/google").permitAll() // OAuth 리다이렉트 경로 허용
                .anyRequest().authenticated()
        );

        // 예외 처리 핸들러 설정
        http.exceptionHandling(configurer ->
                configurer.accessDeniedHandler(jwtAccessDeniedHandler)
        );

        // JwtFilter를 UsernamePasswordAuthenticationFilter 이전에 추가
        http.addFilterBefore(new JwtFilter(jwtUtil,tokenRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
