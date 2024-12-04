package edu.example.wayfarer.controller;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.util.KakaoUtil;
import edu.example.wayfarer.dto.KakaoDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.TokenRepository;
import edu.example.wayfarer.service.AuthService;
import edu.example.wayfarer.auth.util.GoogleUtil;
import edu.example.wayfarer.auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final GoogleUtil googleUtil;
    private final KakaoUtil kakaoUtil;
    private final TokenRepository tokenRepository;

    // Google 로그인 리다이렉트 엔드포인트
    @GetMapping("/login/google")
    public void redirectToGoogle(HttpServletResponse response,
                                 @Value("${GOOGLE_CLIENT_ID}") String clientId,
                                 @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") String redirectUri) throws IOException {
        String googleLoginUrl = "https://accounts.google.com/o/oauth2/auth?"
                + "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("email profile openid", StandardCharsets.UTF_8)
                + "&prompt=consent"; // prompt 파라미터 추가

        log.debug("Redirecting to: " + googleLoginUrl);
        response.sendRedirect(googleLoginUrl);
    }

    @GetMapping("/login/kakao")
    public void redirectToKakao(HttpServletResponse response,
                                 @Value("${KAKAO_CLIENT_ID}") String clientId,
                                 @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String redirectUri) throws IOException {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize?"
                + "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code";//code 값으로 accessCode 반환

        log.debug("Redirecting to: " + kakaoLoginUrl);
        response.sendRedirect(kakaoLoginUrl);
    }

    // Google 및 Kakao 콜백 엔드포인트 통합
    @GetMapping("/{provider}/callback")
    public ResponseEntity<?> socialCallback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String accessCode, //반환된 accessCode 로 각 소셜서버에서 정보가져오기 시작
            HttpServletResponse httpServletResponse,
            @Value("${app.redirect.login.success.url}") String url
    ) {

        Member member;
        if ("google".equalsIgnoreCase(provider)) {
            // 구글 로그인 처리
            try {
                // Authorization Code로 OAUTH Token 가져오기
                String googleAccessToken = googleUtil.getOAuthToken(accessCode);//구글로그인 해서 구글OAUTH 토큰 갖고오기

                // Access Token으로 사용자 정보 가져오기
                GoogleUserInfo userInfo = googleUtil.getUserInfo(googleAccessToken);

                // AuthService로 회원 처리 (쿠키 설정 포함)
                member = authService.googleLogin(userInfo);
                Optional<Token> token = tokenRepository.findByEmail(member.getEmail());
                if (token.isPresent()) {
                    return ResponseEntity.ok(Map.of(
                            "accessToken", token.get().getAccessToken(),
                            "refreshToken", token.get().getRefreshToken()
                    ));
                } else {
                    throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
                }
            } catch (Exception e) {
                log.error("Google Callback Error: {}", e.getMessage());
                throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
            }
        } else if ("kakao".equalsIgnoreCase(provider)) {
            // 카카오 로그인 처리
            try {
                // AuthService로 회원 처리 (쿠키 설정 포함)
                member = authService.kakaoLogin(accessCode);
                Optional<Token> token = tokenRepository.findByEmail(member.getEmail());
                if (token.isPresent()) {
                    return ResponseEntity.ok(Map.of(
                            "accessToken", token.get().getAccessToken(),
                            "refreshToken", token.get().getRefreshToken()
                    ));
                } else {
                    throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
                }
            } catch (Exception e) {
                log.error("Kakao Callback Error: {}", e.getMessage());
                throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
            }
        } else {
            throw new AuthHandler(ErrorStatus._INVALID_PROVIDER);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        try {
            // 헤더에서 Access Token 추출
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AuthHandler(ErrorStatus._TOKEN_NOT_FOUND);
            }
            String accessToken = authorization.split(" ")[1];

            // Access Token 유효성 검증
            try {
                if (!jwtUtil.isAccessTokenValid(accessToken)) {
                    throw new AuthHandler(ErrorStatus._AUTH_EXPIRE_TOKEN);
                }
            } catch (ExpiredJwtException e) {
                // 만료된 Access Token에 대한 별도의 처리
                log.error("Access Token has expired: {}", e.getMessage());
                throw new AuthHandler(ErrorStatus._AUTH_EXPIRE_TOKEN);
            }

            // Access Token에서 이메일 추출
            String email = jwtUtil.getEmail(accessToken);

            // 사용자 토큰 삭제 및 로그아웃 처리 (서비스 레이어에서 처리)
            authService.revokeAndDeleteToken(email);

            // SecurityContextHolder에서 인증 정보 제거
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully logged out"
            ));
        } catch (AuthHandler e) {
            log.error("Logout Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}