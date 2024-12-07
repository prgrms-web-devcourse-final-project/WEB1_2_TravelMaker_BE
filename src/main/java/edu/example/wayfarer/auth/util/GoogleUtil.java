package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.dto.GoogleUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class GoogleUtil {
    @Value("${GOOGLE_CLIENT_ID}")
    private String client_id;
    @Value("${GOOGLE_CLIENT_SECRET}")
    private String client_secret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirect_uri;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getOAuthToken(String authorizationCode) {
        log.info("Authorization Code: {}", authorizationCode); // Authorization Code 로깅

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", client_id);
        body.add("client_secret", client_secret);
        body.add("redirect_uri", redirect_uri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        log.info("Access Token Request: {}", request); // 요청 로깅

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, Map.class);

        log.info("Access Token Response: {}", response.getBody()); // 응답 로깅

        return response.getBody().get("access_token").toString();
    }


    public GoogleUserInfo getUserInfo(String accessToken) {
        // Access Token으로 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.GET, request, GoogleUserInfo.class);

        GoogleUserInfo userInfo = response.getBody();
        userInfo.setGoogleAccessToken(accessToken); // Google Access Token 설정
        return userInfo;
    }

    // 구글 Access Token을 폐기하는 메서드 추가
    public void revokeToken(String oauthToken) {
        String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + oauthToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(revokeUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully revoked token: {}", oauthToken);
            } else {
                log.warn("Failed to revoke token: {}. Status Code: {}", oauthToken, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
        }
    }
}
