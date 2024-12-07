package edu.example.wayfarer.annotation;

import edu.example.wayfarer.dto.responses.DeleteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "회원 탈퇴", responses = {
        @ApiResponse(responseCode = "200", description = "삭제되었습니다.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteResponse.class))),
        @ApiResponse(responseCode = "404", description = "이미 존재하지 않는 회원입니다.",
                content = @Content)
})
public @interface DeleteMemberOperation {
}
