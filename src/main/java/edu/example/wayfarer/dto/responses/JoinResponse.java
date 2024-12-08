package edu.example.wayfarer.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "입장 응답 메시지")
public record JoinResponse (
        @Schema(description = "입장 완료 메시지", example = "방에 입장했습니다.")
        String message
){
}
