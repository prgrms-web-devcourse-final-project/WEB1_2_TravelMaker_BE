package edu.example.wayfarer.auth.filter;

import edu.example.wayfarer.apiPayload.BaseResponse;
import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.auth.userdetails.PrincipalDetails;
import edu.example.wayfarer.auth.util.JwtUtil;
import edu.example.wayfarer.dto.member.MemberRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

// 스프링 시큐리티에 UsernamePasswordAuthenticationFilter 라는게 있음
// /login 요청해서 username,password 전송하면 (POST)
// UsernamePasswordAuthenticationFilter 필터가 작동함

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // /login 요청을 하면, 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        MemberRequestDTO.LoginRequestDTO loginRequestDto = readBody(request);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    private MemberRequestDTO.LoginRequestDTO readBody(HttpServletRequest request) {
        MemberRequestDTO.LoginRequestDTO loginRequestDto = null;
        ObjectMapper om = new ObjectMapper();

        try {
            loginRequestDto = om.readValue(request.getInputStream(), MemberRequestDTO.LoginRequestDTO.class);
        } catch (IOException e) {
            throw new AuthHandler(ErrorStatus._BAD_REQUEST);
        }

        return loginRequestDto;
    }


    // JWT Token 생성해서 response에 담아주기
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException{

        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        String email = principalDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createAccessToken(email, role);

        response.addHeader("Authorization", "Bearer " + token);

        // 성공 응답 통일
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());

        BaseResponse<Object> errorResponse =
                BaseResponse.onSuccess(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
        System.out.println("jwt token : " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        ErrorStatus errorStatus;

        if (failed instanceof UsernameNotFoundException) {
            errorStatus = ErrorStatus._ACCOUNT_NOT_FOUND;
        } else if (failed instanceof BadCredentialsException) {
            errorStatus = ErrorStatus._BAD_CREDENTIALS;
        } else {
            errorStatus = ErrorStatus._AUTHENTICATION_FAILED;
        }
        throw new AuthHandler(errorStatus);
    }
}