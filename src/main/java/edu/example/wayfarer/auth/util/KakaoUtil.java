package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.dto.KakaoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
@Slf4j
public class KakaoUtil {

    @Value("${spring.kakao.auth.client}")
    private String client;
    @Value("${spring.kakao.auth.redirect}")
    private String redirect;
    @Value("${kakao.api.url.mock:false}")
    private boolean isMock;

    public KakaoDTO.OAuthToken requestToken(String accessCode) {
        if (isMock) {
            KakaoDTO.OAuthToken token = new KakaoDTO.OAuthToken();
            token.setAccess_token("mockAccessToken");
            token.setRefresh_token("mockRefreshToken");
            return token;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client);
        params.add("redirect_url", redirect);
        params.add("code", accessCode);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, kakaoTokenRequest, String.class);

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

    public KakaoDTO.KakaoProfile requestProfile(KakaoDTO.OAuthToken oAuthToken){
        RestTemplate restTemplate2 = new RestTemplate();
        HttpHeaders headers2 = new HttpHeaders();

        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers2.add("Authorization","Bearer "+ oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String,String>> kakaoProfileRequest = new HttpEntity <>(headers2);

        ResponseEntity<String> response2 = restTemplate2.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, kakaoProfileRequest, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        KakaoDTO.KakaoProfile kakaoProfile = null;

        try {
            System.out.println(response2.getBody());
            kakaoProfile = objectMapper.readValue(response2.getBody(), KakaoDTO.KakaoProfile.class);
        } catch (JsonProcessingException e) {
            log.info(Arrays.toString(e.getStackTrace()));
            throw new AuthHandler(ErrorStatus._PARSING_ERROR);
        }

        return kakaoProfile;
    }

    // 추가된 메서드: Kakao 토큰 폐기
    public void revokeToken(String accessToken) {
        if (isMock) {
            log.info("Mock revoke token: " + accessToken);
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // 필요한 경우 추가 파라미터 설정

        HttpEntity<MultiValueMap<String, String>> kakaoRevokeRequest = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/logout",
                    HttpMethod.POST,
                    kakaoRevokeRequest,
                    String.class
            );
            log.info("Kakao token revoked: " + response.getBody());
        } catch (Exception e) {
            log.error("Error revoking Kakao token: " + e.getMessage());
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }
    }
}