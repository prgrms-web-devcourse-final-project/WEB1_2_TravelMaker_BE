package edu.example.wayfarer.dto.member;


import lombok.Getter;


public class MemberRequestDTO {

    @Getter
    public static class JoinDTO {
        private String nickname;
        private String email;
        private String password;
        private String role;
        private String profileImage;
    }

    @Getter
    public static class UpdateMemberDTO {
        private String nickname;
    }

    @Getter
    public static class LoginRequestDTO {
        private String email;
        private String password;
    }
}
