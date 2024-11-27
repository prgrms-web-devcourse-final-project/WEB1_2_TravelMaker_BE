package edu.example.wayfarer.service;

import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.util.GoogleUtil;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.auth.util.KakaoUtil;
import edu.example.wayfarer.converter.AuthConverter;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.dto.KakaoDTO;
import edu.example.wayfarer.entity.Token;
import edu.example.wayfarer.repository.MemberRepository;
import edu.example.wayfarer.repository.TokenRepository;
import edu.example.wayfarer.service.AuthService;
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
    public Member kakaoLogin(String accessCode, HttpServletResponse httpServletResponse) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

        Optional<Member> queryUser = memberRepository.findByEmail(kakaoProfile.getKakao_account().getEmail());

        Member member;
        if (queryUser.isPresent()) {
            member = queryUser.get();
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

        // 기존 토큰 삭제
        tokenRepository.deleteByMember_Email(member.getEmail());

        // 새로운 Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getEmail());

        // 토큰 만료 시간 계산
        LocalDateTime accessTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenValiditySeconds());
        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValiditySeconds());

        // 토큰 저장
        Token token = Token.builder()
                .member(member)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .socialAccessToken(oAuthToken.getAccess_token())
                .provider("kakao") // 소셜 제공자 설정
                .accessTokenExpiryDate(accessTokenExpiryDate)
                .refreshTokenExpiryDate(refreshTokenExpiryDate)
                .build();
        tokenRepository.save(token);

        // JWT Access Token과 Refresh Token을 HttpOnly 쿠키에 설정
        setCookie(httpServletResponse, "accessToken", accessToken, jwtUtil.getAccessTokenValiditySeconds(), false);
        setCookie(httpServletResponse, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValiditySeconds(), false);

        return member;
    }

    @Override
    public Member googleLogin(GoogleUserInfo userInfo, HttpServletResponse httpServletResponse) {
        Optional<Member> queryUser = memberRepository.findByEmail(userInfo.getEmail());

        Member member;
        if (queryUser.isPresent()) {
            member = queryUser.get();
        } else {
            String randomPassword = UUID.randomUUID().toString();
            member = AuthConverter.toUser(
                    userInfo.getEmail(),
                    userInfo.getName(),
                    userInfo.getPicture(),
                    randomPassword,
                    passwordEncoder);
            memberRepository.save(member);
        }

        // 기존 토큰 삭제
        tokenRepository.deleteByMember_Email(member.getEmail());

        // 새로운 Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getEmail());

        // Google Access Token 가져오기
        String googleAccessToken = userInfo.getGoogleAccessToken();

        // 토큰 만료 시간 계산 (초 단위)
        LocalDateTime accessTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenValiditySeconds());
        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValiditySeconds());

        // 토큰 저장
        Token token = Token.builder()
                .member(member)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .socialAccessToken(googleAccessToken)
                .provider("google")
                .accessTokenExpiryDate(accessTokenExpiryDate)
                .refreshTokenExpiryDate(refreshTokenExpiryDate)
                .build();
        tokenRepository.save(token);

        // JWT Access Token과 Refresh Token을 HttpOnly 쿠키에 설정
        setCookie(httpServletResponse, "accessToken", accessToken, jwtUtil.getAccessTokenValiditySeconds(), false);
        setCookie(httpServletResponse, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValiditySeconds(), false);

        return member;
    }

    // 공통 쿠키 설정 메서드
    private void setCookie(HttpServletResponse response, String name, String value, long maxAge, boolean isSecure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecure); // 프로덕션 환경에서는 true로 설정
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAge);
        // 쿠키에 SameSite 속성 설정
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly; %s",
                        name, value, maxAge, "/", (isSecure ? "Secure; " : "") + "SameSite=None"));
        response.addCookie(cookie);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }

        // Refresh Token으로 사용자 이메일 추출
        String email = jwtUtil.getEmailFromRefreshToken(refreshToken);
        Optional<Token> optionalToken = tokenRepository.findByRefreshToken(refreshToken);

        if (optionalToken.isEmpty()) {
            throw new AuthHandler(ErrorStatus._AUTH_INVALID_TOKEN);
        }

        Token token = optionalToken.get();

        // Refresh Token 만료 여부 확인
        if (token.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AuthHandler(ErrorStatus._AUTH_EXPIRE_TOKEN);
        }

        // 새로운 Access Token과 Refresh Token 생성
        String newAccessToken = jwtUtil.createAccessToken(email, token.getMember().getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(email);

        // 토큰 만료 시간 계산
        LocalDateTime newAccessTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenValiditySeconds());
        LocalDateTime newRefreshTokenExpiryDate = LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValiditySeconds());

        // 기존 토큰 업데이트 (Social Access Token 및 Provider는 유지)
        token.updateJwtTokens(newAccessToken, newAccessTokenExpiryDate, newRefreshToken, newRefreshTokenExpiryDate);
        tokenRepository.save(token);

        return newAccessToken;
    }

    @Override
    public void revokeAndDeleteToken(String email) throws AuthHandler { // 로그아웃
        // 사용자에게 할당된 토큰 조회
        Optional<Token> optionalToken = tokenRepository.findByMember_Email(email);
        if (optionalToken.isEmpty()) {
            throw new AuthHandler(ErrorStatus._TOKEN_NOT_FOUND);
        }

        Token token = optionalToken.get();

        String socialAccessToken = token.getSocialAccessToken();
        String provider = token.getProvider();

        if (socialAccessToken != null && !socialAccessToken.isEmpty()) {
            if ("google".equalsIgnoreCase(provider)) {
                // 구글 Access Token을 이용하여 로그아웃 처리 (토큰 폐기)
                googleUtil.revokeToken(socialAccessToken);
            } else if ("kakao".equalsIgnoreCase(provider)) {
                // 카카오 Access Token을 이용하여 로그아웃 처리 (토큰 폐기)
                kakaoUtil.revokeToken(socialAccessToken);
            }
        }

        // 토큰 삭제
        tokenRepository.delete(token);
    }
}
