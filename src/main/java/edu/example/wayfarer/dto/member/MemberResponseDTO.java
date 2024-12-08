package edu.example.wayfarer.dto.member;


public record MemberResponseDTO(
        String email,
        String nickname,
        String profileImage
) {
}

