package edu.example.wayfarer.controller;

import edu.example.wayfarer.apiPayload.BaseResponse;
import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.converter.MemberConverter;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.dto.member.MemberResponseDTO;
import edu.example.wayfarer.service.AuthService;
import edu.example.wayfarer.auth.util.GoogleUtil;
import edu.example.wayfarer.auth.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final GoogleUtil googleUtil;

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

    // Google 및 Kakao 콜백 엔드포인트 통합
    @GetMapping("/{provider}/callback")
    public void socialCallback(@PathVariable("provider") String provider,
                                                                        @RequestParam("code") String accessCode,
                                                                        HttpServletResponse httpServletResponse) {
        Member member;
        if ("google".equalsIgnoreCase(provider)) {
            // 구글 로그인 처리
            try {
                // Authorization Code로 Access Token 가져오기
                String googleAccessToken = googleUtil.getAccessToken(accessCode);

                // Access Token으로 사용자 정보 가져오기
                GoogleUserInfo userInfo = googleUtil.getUserInfo(googleAccessToken);

                // AuthService로 회원 처리 (쿠키 설정 포함)
                member = authService.googleLogin(userInfo, httpServletResponse);
            } catch (Exception e) {
                log.error("Google Callback Error: {}", e.getMessage());
                throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
            }
        } else if ("kakao".equalsIgnoreCase(provider)) {
            // 카카오 로그인 처리
            member = authService.kakaoLogin(accessCode, httpServletResponse);
        } else {
            throw new AuthHandler(ErrorStatus._INVALID_PROVIDER);
        }
        //return BaseResponse.onSuccess(MemberConverter.toJoinResultDTO(member));
        try {
            // 로그인 성공 후 메인 페이지로 리다이렉트
            httpServletResponse.sendRedirect("http://localhost:8080/");
        } catch (IOException e) {
            log.error("Redirect Error: {}", e.getMessage());
            throw new AuthHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh Token not found"));
        }

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            String email = jwtUtil.getEmailFromRefreshToken(refreshToken);
            String newRefreshToken = jwtUtil.createRefreshToken(email);

            // 새로운 Access Token과 Refresh Token을 HttpOnly 쿠키에 설정
            setCookie(response, "accessToken", newAccessToken, jwtUtil.getAccessTokenValiditySeconds());
            setCookie(response, "refreshToken", newRefreshToken, jwtUtil.getRefreshTokenValiditySeconds());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken // Refresh Token은 쿠키에 저장되므로 응답 본문에 포함하지 않음
            ));
        } catch (AuthHandler e) {
            log.error("Refresh Token Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // 쿠키에서 Refresh Token 추출 메서드 추가
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // 단일 통합 로그아웃 엔드포인트
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        try {
            // 헤더에서 Access Token 추출
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AuthHandler(ErrorStatus._TOKEN_NOT_FOUND);
            }
            String accessToken = authorization.split(" ")[1];

            // Access Token에서 이메일 추출
            String email = jwtUtil.getEmail(accessToken);

            // 사용자 토큰 삭제 및 로그아웃 처리 (서비스 레이어에서 처리)
            authService.revokeAndDeleteToken(email);

            // HttpOnly 쿠키 삭제
            deleteCookie(response, "accessToken");
            deleteCookie(response, "refreshToken");

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

    // 쿠키 설정 메서드
    private void setCookie(HttpServletResponse response, String name, String value, long maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 프로덕션에서는 true로 설정
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAge);
        response.addCookie(cookie);
    }

    // 쿠키 삭제 메서드
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 프로덕션에서는 true로 설정
        cookie.setPath("/");
        cookie.setMaxAge(0); // 삭제
        response.addCookie(cookie);
    }
}
