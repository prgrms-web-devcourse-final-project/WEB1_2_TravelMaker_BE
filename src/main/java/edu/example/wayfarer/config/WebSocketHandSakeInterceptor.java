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
            //요청 URL에서 access_token 쿼리 파라미터 추출
            String query = request.getURI().getQuery();
            log.info("query: " + query);
            String jwtToken = getAccessTokenFromQueryParam(query);
            log.info("JWT token: " + jwtToken);

            //JWT에서 이메일 추출
            String email = jwtUtil.getEmail(jwtToken);

            if( email == null) {
                log.error("Email is missing in the JWT");
                throw new WebSocketTaskException(WebSocketException.INVALID_EMAIL);
            }

            log.info("email: " + email);
            //세션에 이메일 추가
            attributes.put("email", email);
            return true;
        } catch (WebSocketTaskException e) {
            response.setStatusCode(HttpStatusCode.valueOf(e.getCode()));
            return false;
        } catch (Exception e) {
            log.error("Exception during WebSocket handshake: " + e.getMessage(), e);
            response.setStatusCode(HttpStatusCode.valueOf(400));
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private String getAccessTokenFromQueryParam(String queryParam) {

        // 요청 헤더에서 Authorization 추출
        if (queryParam == null || !queryParam.startsWith("access_token=")) {
            throw new WebSocketTaskException(WebSocketException.INVALID_TOKEN);
        }
        return queryParam.substring("access_token=".length());
    }
}
