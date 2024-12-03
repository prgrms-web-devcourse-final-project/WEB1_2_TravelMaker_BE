package edu.example.wayfarer.config;

import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
@Log4j2
public class WebSocketHandSakeInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try{
            //요청 쿠키에서 accessToken 추출
            String jwtToken = getAccessTokenFromCookies(request);
            log.info("JWT token: " + jwtToken);
            if (jwtToken == null) {
                log.error("JWT Token is missing in cookies!");
                return false;  // JWT가 없으면 연결 거부
            }
            //JWT에서 이메일 추출
            String email = jwtUtil.getEmail(jwtToken);
            log.info("email: " + email);

            //세션에 이메일 추가
            attributes.put("email", email);

            return true;
        } catch (WebSocketTaskException e) {
            response.setStatusCode(HttpStatusCode.valueOf(e.getCode()));
            return false;
        } catch (Exception e) {
            log.error("Exception during WebSocket handshake: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String getAccessTokenFromCookies(ServerHttpRequest request) {
        // 요청 헤더에서 COOKIE 값 추출
        String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        log.info("cookieHeader: " + cookieHeader);

        if (cookieHeader != null) {
            // 쿠키 값들을 ; 기준으로 나누기
            String[] cookies = cookieHeader.split(";");

            // 각각의 쿠키에서 'accessToken=' 값을 찾아서 반환
            for (String cookie : cookies) {
                // 각 쿠키에서 양쪽 공백을 제거하고, 'accessToken='으로 시작하는지 확인
                if (cookie.trim().startsWith("accessToken=")) {
                    // '=' 뒤의 value 값 추출
                    String token = cookie.split("=")[1].trim();
                    log.info("token: " + token);
                    return token;
                }
            }
        }
        log.info("잘못됐다");
        return null; // 'accessToken'이 없거나 쿠키가 없는 경우 null 반환
    }
}
