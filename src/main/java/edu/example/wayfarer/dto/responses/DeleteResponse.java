package edu.example.wayfarer.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "삭제 응답 메시지")
public record DeleteResponse(
        @Schema(description = "삭제 완료 메시지", example = "삭제되었습니다.")
        String message
) {}
