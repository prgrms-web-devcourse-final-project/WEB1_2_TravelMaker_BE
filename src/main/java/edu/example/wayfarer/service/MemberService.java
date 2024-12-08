package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.member.MemberResponseDTO;
import edu.example.wayfarer.dto.member.MemberUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    MemberResponseDTO read(String email);

    MemberResponseDTO updateNickname(MemberUpdateDTO memberUpdateDTO, String email);

    MemberResponseDTO updateImg(String email, MultipartFile newImage);

    void delete(String email);
}
