package edu.example.wayfarer.annotation;

import edu.example.wayfarer.dto.responses.DeleteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "방 삭제 (방장만 가능)", responses = {
        @ApiResponse(responseCode = "200", description = "삭제되었습니다.",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteResponse.class))),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다", content = @Content),
        @ApiResponse(responseCode = "401", description = "권한이 없습니다", content = @Content)
})
public @interface DeleteRoomOperation {
}
