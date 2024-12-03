package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.apiPayload.exception.AuthorizationException;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final MemberRepository memberRepository;

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //사용자의 인증 정보를 담는다 (스레드)
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationException("인증 정보가 없습니다.");
        }
        return authentication;
    }

    public Member getCurrentUser() {
        Authentication authentication = getAuthentication();
        String email = (String) authentication.getPrincipal(); //jwt토큰을 통해 claim(페이로드를 담은 객체)부분에서 애플리케이션이 id또는 email같은 주체정보 꺼내오기 가능.
        // 소셜로그인이라서 Principal(주체정보)로 이메일을 뽑아냄

        // MemberRepository를 이용해 현재 인증된 사용자의 이메일로 Member 객체를 찾습니다.
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthorizationException("인증된 사용자 정보를 확인할 수 없습니다."));
    }
}
