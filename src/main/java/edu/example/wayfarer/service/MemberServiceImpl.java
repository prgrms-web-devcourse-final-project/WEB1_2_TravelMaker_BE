package edu.example.wayfarer.service;

import edu.example.wayfarer.converter.MemberConverter;
import edu.example.wayfarer.dto.member.MemberResponseDTO;
import edu.example.wayfarer.dto.member.MemberUpdateDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.exception.MemberException;
import edu.example.wayfarer.repository.MemberRepository;
import edu.example.wayfarer.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final MemberRoomServiceImpl memberRoomServiceImpl;
    private final S3Service s3Service;

    @Override
    public MemberResponseDTO read(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberException.INFO_NOT_FOUND::get);
        return MemberConverter.toMemberResponseDTO(member);
    }

    @Override
    public MemberResponseDTO updateNickname(MemberUpdateDTO memberUpdateDTO, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberException.NOT_FOUND::get);
        member.changeNickname(memberUpdateDTO.nickname());
        memberRepository.save(member);
        return MemberConverter.toMemberResponseDTO(member);
    }

    @Override
    public MemberResponseDTO updateImg(String email, MultipartFile newImage) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberException.NOT_FOUND::get);

        // 존재하는 기존이미지가 S3에 있다면 삭제
        String oldImageUrl = member.getProfileImage();
        if(oldImageUrl != null) {
            s3Service.deleteFileFromS3(oldImageUrl);
        }

        String newImageUrl = s3Service.upload(email , newImage);
        member.changeImage(newImageUrl);
        memberRepository.save(member);

        return MemberConverter.toMemberResponseDTO(member);
    }

    @Override
    public void delete(String email) {
        Optional<Room> foundRoom = roomRepository.findByHostEmail(email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberException.NOT_FOUND::get);

        // 만약 탈퇴한 회원이 host인 방이 있다면 host가 바뀌도록
        if(foundRoom.isPresent()){
            Room room = foundRoom.get();
            memberRoomServiceImpl.hostExit(member, room);
        }
        memberRepository.deleteById(email);
    }


}
