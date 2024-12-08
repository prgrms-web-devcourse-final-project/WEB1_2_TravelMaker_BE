package edu.example.wayfarer.service;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.util.GoogleUtil;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.auth.util.KakaoUtil;
import edu.example.wayfarer.converter.AuthConverter;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.dto.KakaoDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.MemberRepository;
import edu.example.wayfarer.repository.TokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final KakaoUtil kakaoUtil;
    private final GoogleUtil googleUtil;
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member kakaoLogin(String accessCode) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.getAccessToken(accessCode);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.getUserInfo(oAuthToken);

        Optional<Member> queryUser = memberRepository.findByEmail(kakaoProfile.getKakao_account().getEmail());

        Member member;
        if (queryUser.isPresent()) {
            member = queryUser.get();

            // 기존에 존재하는 토큰이 있다면 폐기
            tokenRepository.findByEmail(member.getEmail()).ifPresent(token -> {
                kakaoUtil.revokeToken(token.getSocialAccessToken()); // 기존 토큰 폐기
                tokenRepository.deleteByEmail(member.getEmail()); // Redis에서도 토큰 삭제
            });

        } else {
            String randomPassword = UUID.randomUUID().toString();
            member = AuthConverter.toUser(
                    kakaoProfile.getKakao_account().getEmail(),
                    kakaoProfile.getKakao_account().getProfile().getNickname(),
                    kakaoProfile.getKakao_account().getProfile().getProfile_image_url(),
                    randomPassword,
                    passwordEncoder);
            memberRepository.save(member);
        }

        // JWT 토큰 생성 및 Redis 저장 로직을 JwtUtil로 이동
        jwtUtil.generateAndStoreTokens(member.getEmail(), member.getRole(), oAuthToken.getAccess_token(), "kakao");

        return member;
    }

    @Override
    public Member googleLogin(GoogleUserInfo userInfo) {
        Optional<Member> queryUser = memberRepository.findByEmail(userInfo.getEmail());//email로 기존에 존재하던 사람인지 옵셔널

        Member member;
        if (queryUser.isPresent()) {
            member = queryUser.get();

            // 기존에 존재하는 토큰이 있다면 폐기
            tokenRepository.findByEmail(member.getEmail()).ifPresent(token -> {
                googleUtil.revokeToken(token.getSocialAccessToken()); // 기존 토큰 폐기 ->로그인에서 다시 로그인한 것
                tokenRepository.deleteByEmail(member.getEmail()); // Redis에서도 토큰 삭제
            });

        } else {//회원가입
            String randomPassword = UUID.randomUUID().toString();
            member = AuthConverter.toUser(
                    userInfo.getEmail(),
                    userInfo.getName(),
                    userInfo.getPicture(),
                    randomPassword,
                    passwordEncoder);
            memberRepository.save(member);
        }

        // JWT 토큰 생성 및 Redis 저장 로직을 JwtUtil로 이동
        jwtUtil.generateAndStoreTokens(member.getEmail(), member.getRole(), userInfo.getGoogleAccessToken(), "google");

        return member;
    }

    // AuthServiceImpl.java
    @Override
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }

        // Refresh Token으로 사용자 이메일 추출
        String email = jwtUtil.getEmail(refreshToken);
        Optional<Token> optionalToken = tokenRepository.findByRefreshToken(refreshToken);

        if (optionalToken.isEmpty()) {
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }

        Token token = optionalToken.get();

        // 새로운 Access Token과 Refresh Token 생성
        String newAccessToken = jwtUtil.createAccessToken(email, token.getProvider());
        String newRefreshToken = jwtUtil.createRefreshToken(email);

        // 토큰 만료 시간 계산
        LocalDateTime newAccessTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenValiditySeconds());
        LocalDateTime newRefreshTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValiditySeconds());

        // 기존 토큰 업데이트
        token.updateJwtTokens(newAccessToken, newAccessTokenExpiryDate, newRefreshToken, newRefreshTokenExpiryDate);
        tokenRepository.save(token);

        return newAccessToken;
    }

    @Override
    public void revokeAndDeleteToken(String email) throws AuthHandler { //소셜서버에 토큰 삭제 알리기 및 우리 토큰 삭제
        Optional<Token> optionalToken = tokenRepository.findByEmail(email);
        if (optionalToken.isEmpty()) {
            throw new AuthHandler(ErrorStatus._TOKEN_NOT_FOUND);
        }

        Token token = optionalToken.get();

        if(token.getProvider().equals("google")) {
            // Google Access Token 폐기
            googleUtil.revokeToken(token.getSocialAccessToken());
        }else{
            kakaoUtil.revokeToken(token.getSocialAccessToken());
        }

        // 토큰 삭제
        tokenRepository.delete(token);
    }
}
