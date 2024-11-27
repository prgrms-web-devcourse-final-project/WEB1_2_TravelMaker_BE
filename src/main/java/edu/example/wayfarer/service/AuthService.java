package edu.example.wayfarer.service;

import edu.example.wayfarer.apiPayload.exception.handler.AuthHandler;
import edu.example.wayfarer.dto.GoogleUserInfo;
import edu.example.wayfarer.entity.Member;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    Member googleLogin(GoogleUserInfo userInfo, HttpServletResponse httpServletResponse);
    Member kakaoLogin(String accessCode, HttpServletResponse httpServletResponse);
    String refreshAccessToken(String refreshToken);
    void revokeAndDeleteToken(String email) throws AuthHandler;
}