package edu.example.wayfarer.auth.util;

import edu.example.wayfarer.apiPayload.exception.AuthorizationException;
import edu.example.wayfarer.auth.userdetails.PrincipalDetails;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.dto.KakaoDTO;
import edu.example.wayfarer.dto.KakaoDTO.KakaoProfile;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final MemberRepository memberRepository;

    private Authentication getAuthentication(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new AuthorizationException("인증 정보가 없습니다.");
        }
        return authentication;
    }

    public Member getCurrentUser(){
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();

        if(principal instanceof PrincipalDetails){
            PrincipalDetails principalDetails = (PrincipalDetails) principal;
            return principalDetails.getMember();
        } else{
            throw new AuthorizationException("인증된 사용자 정보를 확인할 수 없습니다.");
        }
    }
}
