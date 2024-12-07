package edu.example.wayfarer.apiPayload.exception;

import edu.example.wayfarer.apiPayload.BaseResponse;
import edu.example.wayfarer.apiPayload.code.ErrorReasonDTO;
import edu.example.wayfarer.apiPayload.code.status.ErrorStatus;
import edu.example.wayfarer.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {


    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(constraintViolation -> constraintViolation.getMessage())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, ErrorStatus.valueOf(errorMessage), HttpHeaders.EMPTY,request);
    }


    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().stream()
                .forEach(
                        fieldError -> {
                            String fieldName = fieldError.getField();
                            String errorMessage =
                                    Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                            errors.merge(
                                    fieldName,
                                    errorMessage,
                                    (existingErrorMessage, newErrorMessage) ->
                                            existingErrorMessage + ", " + newErrorMessage);
                        });

        return handleExceptionInternalArgs(
                e, HttpHeaders.EMPTY, ErrorStatus.valueOf("_BAD_REQUEST"), request, errors);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();

        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity onThrowException(GeneralException generalException, HttpServletRequest request) {
        ErrorReasonDTO errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
        return handleExceptionInternal(generalException,errorReasonHttpStatus,null,request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorReasonDTO reason,
                                                           HttpHeaders headers, HttpServletRequest request) {

        BaseResponse<Object> body = BaseResponse.onFailure(reason.getCode(),reason.getMessage(),null);
//        e.printStackTrace();

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                reason.getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorStatus errorCommonStatus,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {
        BaseResponse<Object> body = BaseResponse.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorStatus errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        BaseResponse<Object> body = BaseResponse.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, ErrorStatus errorCommonStatus,
                                                                     HttpHeaders headers, WebRequest request) {
        BaseResponse<Object> body = BaseResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    @ExceptionHandler(TaskException.class)
    public ResponseEntity<?> handleTaskException(TaskException e){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", e.getStatus());
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(e.getStatus())
                .body(errorResponse);
    }

//    @ExceptionHandler(TaskException.class)
//    public ResponseEntity<?> handleGeocodingTaskException(TaskException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error message", e.getMessage());
//        errorResponse.put("status", e.getStatus());
//        errorResponse.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity
//                .status(e.getStatus())
//                .body(errorResponse);
//    }
//
//    @ExceptionHandler(TaskException.class)
//    public ResponseEntity<?> handleScheduleItemTaskException(TaskException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error message", e.getMessage());
//        errorResponse.put("status", e.getStatus());
//        errorResponse.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity
//                .status(e.getStatus())
//                .body(errorResponse);
//    }
//
//    @ExceptionHandler(TaskException.class)
//    public ResponseEntity<?> handleMarkerTaskException(TaskException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error message", e.getMessage());
//        errorResponse.put("status", e.getStatus());
//        errorResponse.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity
//                .status(e.getStatus())
//                .body(errorResponse);
//    }
//
//    @ExceptionHandler(TaskException.class)
//    public ResponseEntity<?> handleScheduleTaskException(TaskException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error message", e.getMessage());
//        errorResponse.put("status", e.getStatus());
//        errorResponse.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity
//                .status(e.getStatus())
//                .body(errorResponse);
//    }
//
//    @ExceptionHandler(TaskException.class)
//    public ResponseEntity<?> handleRoomTaskException(TaskException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error message", e.getMessage());
//        errorResponse.put("status", e.getStatus());
//        errorResponse.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity
//                .status(e.getStatus())
//                .body(errorResponse);
//    }
}
