package edu.example.wayfarer.auth.constant;

import java.util.Arrays;
import java.util.stream.Stream;

public class SecurityConstants {

    public static final String[] swaggerUrls = {"/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**"};
    public static final String[] allowUrls = {
            "/api/v1/posts/**",
            "/api/v1/replies/**",
            "/login",
            "/auth/kakao/callback/**",
            "/auth/google/callback/**",
            "/auth/login/google", // 구글 경로 추가
            "/auth/login/kakao",
            //"/login/oauth2/code/google", // OAuth 리다이렉트 경로 추가
            "/logout",
            "/refresh",
            //"/favicon.ico",
            //"/auth/logout",
            "/auth/refresh",
            "/index.html"
            //"/", // 메인페이지
     };

    // 허용 Urls
    public static String[] allowedUrls = Stream.concat(Arrays.stream(swaggerUrls), Arrays.stream(allowUrls))
            .toArray(String[]::new);

}
