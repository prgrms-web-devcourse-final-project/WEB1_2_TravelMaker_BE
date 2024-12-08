package edu.example.wayfarer.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("Token") // Redis에 저장될 Hash 이름
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Token implements Serializable {
    @Id
    private String email; // Redis에서는 ID를 고유 식별자로 사용

    @Indexed // 조회에 빠르게 사용할 수 있도록 인덱스 설정
    private String accessToken;

    private String refreshToken;

    private LocalDateTime accessTokenExpiryDate;
    private LocalDateTime refreshTokenExpiryDate;

    // 변경된 필드: Social Access Token
    private String socialAccessToken;

    // 추가된 필드: 소셜 제공자 구분 (예: "google", "kakao")
    private String provider;

    // JWT 토큰 업데이트 메서드
    public void updateJwtTokens(String newAccessToken, LocalDateTime newAccessTokenExpiryDate,
                                String newRefreshToken, LocalDateTime newRefreshTokenExpiryDate) {
        this.accessToken = newAccessToken;
        this.accessTokenExpiryDate = newAccessTokenExpiryDate;
        this.refreshToken = newRefreshToken;
        this.refreshTokenExpiryDate = newRefreshTokenExpiryDate;
    }

    // Social Access Token 업데이트 메서드
    public void updateSocialAccessToken(String newSocialAccessToken) {
        this.socialAccessToken = newSocialAccessToken;
    }
}
