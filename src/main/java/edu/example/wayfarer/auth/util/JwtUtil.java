package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
@Getter
public class JwtUtil {
    private SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;
    private final TokenRepository tokenRepository;

    @Value("${spring.jwt.secret}")
    private String secretKeyString;

    public JwtUtil(
            @Value("${spring.jwt.access-token-time}") final long accessTokenValiditySeconds,
            @Value("${spring.jwt.refresh-token-time}") final long refreshTokenValiditySeconds, TokenRepository tokenRepository) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        this.tokenRepository = tokenRepository;
    }

    @PostConstruct
    private void initializeSecretKey() {
        if (secretKeyString == null || secretKeyString.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured properly");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // HTTP 요청의 'Authorization' 헤더 또는 쿠키에서 JWT 액세스 토큰을 검색
    public String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            log.info("[*] Token found in header");
            return authorization.split(" ")[1];
        }

        // 헤더에 없으면 쿠키에서 검색
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    log.info("[*] Access Token found in cookies: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        log.warn("[*] No Access Token found in request");
        return null;
    }

    // HTTP 요청의 'Authorization' 헤더 또는 쿠키에서 JWT 리프레시 토큰을 검색
    public String resolveRefreshToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            log.info("[*] Token found in header");
            return authorization.split(" ")[1];
        }

        // 헤더에 없으면 쿠키에서 검색
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    log.info("[*] Refresh Token found in cookies: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        log.warn("[*] No Access Token found in request");
        return null;
    }

    // JWT 토큰 발급 및 Redis 저장
    public void generateAndStoreTokens(String email, String role, String socialAccessToken, String provider) {
        String accessToken = createAccessToken(email, role);
        String refreshToken = createRefreshToken(email);

        LocalDateTime accessTokenExpiryDate = LocalDateTime.now().plusSeconds(accessTokenValiditySeconds);
        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);

        // 새로운 토큰 객체 생성 및 저장
        Token token = Token.builder()
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiryDate(accessTokenExpiryDate)
                .refreshTokenExpiryDate(refreshTokenExpiryDate)
                .socialAccessToken(socialAccessToken)
                .provider(provider)
                .build();

        // Redis에 토큰 저장
        tokenRepository.save(token);
//
//        // 쿠키 설정
//        setAccessTokenCookie(response, accessToken); //수정필요
//        setRefreshTokenCookie(response, refreshToken);
    }

    // Access Token 생성
    public String createAccessToken(String email, String role) {
        return createToken(email, role, accessTokenValiditySeconds);
    }

    // Refresh Token 생성
    public String createRefreshToken(String email) {
        return createToken(email, null, refreshTokenValiditySeconds);
    }

    private String createToken(String email, String role, long validitySeconds) {
        //Claims 는 JWT의 헤더,페이로드,서명 중 페이로드 정보를 담고 있는 객체를 의미합니다.
        Claims claims = Jwts.claims();
        claims.put("email", email);
        if (role != null) {
            claims.put("role", role);
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(validitySeconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //JWT 토큰을 파싱하여 Jws<Claims>를 반환
    private Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder() //Jwts는 JWT를 생성하거나 파싱할 수 있도록 도와주는 역할.
                .setSigningKey(secretKey)//서명이 올바른지 검증하기 위해 우리가 설정한 secretKey를 세팅
                .build().parseClaimsJws(token);//setSigningKey로 설정된 비밀 키를 사용하여 토큰의 서명을 검증하고, 토큰이 유효한지 확인
    }

    // 만료된 토큰에서 이메일 추출
    public String getEmailFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("email", String.class);
        } catch (ExpiredJwtException e) {
            log.info("[*] 만료된 토큰에서 이메일을 추출합니다.");
            return e.getClaims().get("email", String.class);
        } catch (JwtException e) {
            log.error("[*] 토큰에서 이메일을 추출하는 도중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // Access Token에서 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).getBody()//페이로드 데이터에 접근
                .get("email", String.class);
    }

    public boolean isAccessTokenValid(String token) {
        return validateToken(token);
    }

    // Refresh Token 유효성 검증
    public boolean isRefreshTokenValid(String token) {
        return validateToken(token);
    }

    // 토큰 검증 메서드 -> 기한으로 따지기
    private boolean validateToken(String token) {
        try {
            Jws<Claims> claims = getClaims(token);
            Date expiredDate = claims.getBody().getExpiration();
            Date now = new Date();
            return expiredDate.after(now);
        } catch (JwtException e) {
            log.error("[*] Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Authentication createAuthentication(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

}
