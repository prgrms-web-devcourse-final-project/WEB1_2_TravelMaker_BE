package edu.example.wayfarer.converter;


import edu.example.wayfarer.entity.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthConverter {

    public static Member toUser(String email, String nickname,String profileImage, String password, PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .role("ROLE_USER")
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
}
