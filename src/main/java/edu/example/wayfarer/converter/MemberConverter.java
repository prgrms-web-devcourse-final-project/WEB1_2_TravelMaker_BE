package edu.example.wayfarer.converter;

import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.dto.member.MemberRequestDTO;
import edu.example.wayfarer.dto.member.MemberResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class MemberConverter {

    public static Member toMember(MemberRequestDTO.JoinDTO joinDTO, PasswordEncoder passwordEncoder) {
        return Member.builder()
                .nickname(joinDTO.getNickname())
                .password(passwordEncoder.encode(joinDTO.getPassword()))
                .email(joinDTO.getEmail())
                .profileImage(joinDTO.getProfileImage())
                .role(joinDTO.getRole())
                .build();
    }

    public static MemberResponseDTO.JoinResultDTO toJoinResultDTO(Member member) {
        return MemberResponseDTO.JoinResultDTO.builder()
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.MemberPreviewDTO toMemberPreviewDTO(Member member) {
        return MemberResponseDTO.MemberPreviewDTO.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .updatedAt(member.getUpdatedAt())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static MemberResponseDTO.MemberPreviewListDTO toMemberPreviewListDTO(List<Member> memberList) {
        List<MemberResponseDTO.MemberPreviewDTO> memberPreviewDTOList = memberList.stream()
                                                                    .map(MemberConverter::toMemberPreviewDTO)
                                                                    .toList();

        return MemberResponseDTO.MemberPreviewListDTO.builder()
                .memberPreviewDTOList(memberPreviewDTOList)
                .build();
    }
}
