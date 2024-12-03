package edu.example.wayfarer.auth.filter;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.constant.SecurityConstants;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.TokenRepository;
import edu.example.wayfarer.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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

        if (accessToken != null && jwtUtil.isAccessTokenValid(accessToken)) {
            // 유효한 Access Token인 경우, SecurityContext에 인증 정보 설정
            String email = jwtUtil.getEmail(accessToken);
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.createAuthentication(email));
        } else {
            log.info("[*] Access Token이 만료되었거나 존재하지 않습니다. Refresh Token으로 재발급을 시도합니다.");

            // Access Token이 만료된 경우, Refresh Token을 이용해 재발급을 시도
            String refreshToken = jwtUtil.resolveRefreshToken(request);

            if (refreshToken != null && jwtUtil.isRefreshTokenValid(refreshToken)) {
                String email = jwtUtil.getEmail(refreshToken);

                // 기존 Token 정보 가져오기
                Optional<Token> tokenOptional = tokenRepository.findByEmail(email);

                if (tokenOptional.isPresent()) {
                    Token existingToken = tokenOptional.get();

                    // generateAndStoreTokens를 이용해 Access Token과 Refresh Token을 재발급하고 저장
                    jwtUtil.generateAndStoreTokens(email,"ROLE_USER", existingToken.getSocialAccessToken(),existingToken.getProvider(), response);

                    // SecurityContext에 새로운 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(jwtUtil.createAuthentication(email));
                } else {
                    log.warn("[*] 유효한 토큰을 찾을 수 없습니다.");
                }
            } else {
                log.warn("[*] Refresh Token이 유효하지 않습니다. 인증에 실패했습니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

//    private void handleAuthError(AuthHandler e, HttpServletResponse response) throws IOException {
//        if (ErrorStatus._AUTH_EXPIRE_TOKEN.equals(e.getErrorReasonHttpStatus().getCode())) {
//            log.error("Access Token expired: {}", e.getMessage());
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            response.getWriter().write("Unauthorized - Access Token expired");
//        } else {
//            log.error("Authentication Error: {}", e.getMessage());
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            response.getWriter().write("Unauthorized");
//        }
//    }

}
