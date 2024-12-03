package edu.example.wayfarer.annotation;

import edu.example.wayfarer.dto.responses.LeaveResponse;
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
@Operation(summary = "방 퇴장", responses = {
        @ApiResponse(responseCode = "200", description = "퇴장하였습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveResponse.class)))
})
public @interface LeaveOperation {
}
