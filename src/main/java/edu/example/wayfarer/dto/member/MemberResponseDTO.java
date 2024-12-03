//package edu.example.wayfarer.dto.member;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public class MemberResponseDTO {
//
//    @Getter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class JoinResultDTO {
//        private String email;
//        private LocalDateTime createdAt;
//    }
//
//    @Getter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class MemberPreviewDTO {
//        private String email;
//        private String nickname;
//        private String profileImage;
//        private LocalDateTime updatedAt;
//        private LocalDateTime createdAt;
//    }
//
//    @Getter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class MemberPreviewListDTO {
//        List<MemberPreviewDTO> memberPreviewDTOList;
//    }
//}
