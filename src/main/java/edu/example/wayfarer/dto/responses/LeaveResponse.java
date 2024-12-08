package edu.example.wayfarer.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "퇴장 응답 메시지")
public record LeaveResponse(
        @Schema(description = "퇴장 완료 메시지", example = "퇴장하였습니다.")
        String message
) {
}
