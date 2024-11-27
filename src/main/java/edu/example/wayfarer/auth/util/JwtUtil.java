package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
@Slf4j
@Getter
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtUtil(
            @Value("${spring.jwt.secret}") final String secretKey,
            @Value("${spring.jwt.access-token-time}") final long accessTokenValiditySeconds,
            @Value("${spring.jwt.refresh-token-time}") final long refreshTokenValiditySeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
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
                    log.info("[*] Token found in cookies: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        log.warn("[*] No Access Token found in request");
        return null;
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

    // Access Token에서 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).getBody().get("email", String.class);
    }

    // Refresh Token에서 이메일 추출
    public String getEmailFromRefreshToken(String token) {
        return getClaims(token).getBody().get("email", String.class);
    }

    // Access Token 유효성 검증
    public boolean isAccessTokenValid(String token) {
        return validateToken(token);
    }

    // Refresh Token 유효성 검증
    public boolean isRefreshTokenValid(String token) {
        return validateToken(token);
    }

    // 토큰 검증 메서드
    private boolean validateToken(String token) {
        try {
            Jws<Claims> claims = getClaims(token);
            Date expiredDate = claims.getBody().getExpiration();
            Date now = new Date();
            return expiredDate.after(now);
        } catch (ExpiredJwtException e) {
            log.info("[*] _AUTH_EXPIRE_TOKEN");
            throw new AuthHandler(ErrorStatus._AUTH_EXPIRE_TOKEN);
        } catch (SignatureException
                 | SecurityException
                 | IllegalArgumentException
                 | MalformedJwtException
                 | UnsupportedJwtException e) {
            log.info("[*] _AUTH_INVALID_TOKEN");
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
    }


}
