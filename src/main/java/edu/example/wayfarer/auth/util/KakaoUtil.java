package edu.example.wayfarer.auth.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.dto.KakaoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
public class KakaoUtil {
    @Value("${KAKAO_CLIENT_ID}")
    private String client_id;
    @Value("${KAKAO_CLIENT_SECRET}")
    private String client_secret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirect_uri;
    private final RestTemplate restTemplate = new RestTemplate();//REST API 호출을 위한 객체

    public KakaoDTO.OAuthToken getAccessToken(String authorizationCode) {
        log.info("Authorization Code: {}", authorizationCode);

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

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoDTO.OAuthToken oAuthToken = null;

        try {
            oAuthToken = objectMapper.readValue(response.getBody(), KakaoDTO.OAuthToken.class);
            log.info("oAuthToken : " + oAuthToken.getAccess_token());
        } catch (JsonProcessingException e) {
            throw new AuthHandler(ErrorStatus._PARSING_ERROR);
        }
        return oAuthToken;
    }

    public KakaoDTO.KakaoProfile getUserInfo(KakaoDTO.OAuthToken oAuthToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity <>(headers);//헤더만 포함된 HttpEntity 객체생성

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        KakaoDTO.KakaoProfile kakaoProfile = null;
        try {
            System.out.println(response.getBody());
            kakaoProfile = objectMapper.readValue(response.getBody(), KakaoDTO.KakaoProfile.class);
        } catch (JsonProcessingException e) {
            log.info(Arrays.toString(e.getStackTrace()));
            throw new AuthHandler(ErrorStatus._PARSING_ERROR);
        }
        return kakaoProfile;
    }

    // 추가된 메서드: Kakao 토큰 폐기
    public void revokeToken(String oauthToken) {
        String revokeUrl = "https://kapi.kakao.com/v1/user/logout"; // 카카오 Access Token 폐기 API URL

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + oauthToken); // Bearer 형식으로 토큰 전달

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