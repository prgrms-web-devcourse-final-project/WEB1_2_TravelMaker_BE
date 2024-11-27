package edu.example.wayfarer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자와의 연관관계 설정 (OneToOne)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false, unique = true)
    private String accessToken;

    @Column(nullable = false, unique = true)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime accessTokenExpiryDate;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpiryDate;

    // 변경된 필드: Social Access Token
    @Column(nullable = true, unique = true)
    private String socialAccessToken;

    // 추가된 필드: 소셜 제공자 구분 (예: "google", "kakao")
    @Column(nullable = false)
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
