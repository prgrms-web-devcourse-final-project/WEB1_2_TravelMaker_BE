package edu.example.wayfarer.converter;

import edu.example.wayfarer.entity.Member;
//import edu.example.wayfarer.dto.member.MemberRequestDTO;
import edu.example.wayfarer.dto.member.MemberResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class MemberConverter {

    public static MemberResponseDTO toMemberResponseDTO(Member member) {
        return new MemberResponseDTO(
                member.getEmail(),
                member.getNickname(),
                member.getProfileImage()
        );
    }
}

