package edu.example.wayfarer.auth.filter;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.constant.SecurityConstants;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.TokenRepository;
import edu.example.wayfarer.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldSkip = Arrays.stream(SecurityConstants.allowedUrls)
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));

        log.info("Should skip JwtFilter for path {}: {}", path, shouldSkip);
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.resolveAccessToken(request);

        if (accessToken != null) {
            try {
                if (jwtUtil.isAccessTokenValid(accessToken)) {
                    // 유효한 Access Token인 경우, SecurityContext에 인증 정보 설정
                    String email = jwtUtil.getEmail(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(jwtUtil.createAuthentication(email));
                } else {
                    throw new ExpiredJwtException(null, null, "Access token expired");
                }
            } catch (ExpiredJwtException e) {
                log.info("[*] Access Token이 만료되었습니다. Refresh Token으로 재발급을 시도합니다.");
                String email = jwtUtil.getEmailFromExpiredToken(accessToken); // 만료된 토큰에서 이메일 추출
                if (email != null) {
                    Optional<Token> tokenOptional = tokenRepository.findByEmail(email);
                    if (tokenOptional.isPresent()) {
                        String refreshToken = tokenOptional.get().getRefreshToken();
                        if (refreshToken != null && jwtUtil.isRefreshTokenValid(refreshToken)) {
                            jwtUtil.generateAndStoreTokens(email, "ROLE_USER", null, null);
                            SecurityContextHolder.getContext().setAuthentication(jwtUtil.createAuthentication(email));
                        } else {
                            log.warn("[*] Refresh Token이 유효하지 않습니다. 인증에 실패했습니다.");
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("Unauthorized - Token expired or invalid");
                            return; // 필터 체인을 중단하고 응답을 반환합니다.
                        }
                    } else {
                        log.warn("[*] 이메일에 해당하는 토큰을 찾을 수 없습니다.");
                    }
                } else {
                    log.warn("[*] 만료된 토큰에서 이메일을 추출하지 못했습니다.");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("Unauthorized - Token expired or invalid");
                    return;
                }
            }
        } else {
            log.warn("[*] No Access Token found in request");
        }

        filterChain.doFilter(request, response);
    }

}
