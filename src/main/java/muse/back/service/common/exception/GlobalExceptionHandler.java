package muse.back.service.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import web.common.core.response.base.dto.ResponseErrorDTO;
import web.common.core.response.base.vo.Code;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MuseException.class)
    public ResponseEntity<ResponseErrorDTO> handleMuseException(
            MuseException ex,
            WebRequest request) {
        log.warn("MuseException: code={}, status={}, message={}", ex.getCode(), ex.getStatus(), ex.getMessage());
        Code mappedCode = mapCode(ex.getCode());
        ResponseErrorDTO errorResponse = ResponseErrorDTO.of(mappedCode, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseErrorDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        var fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> String.format("%s=%s (%s)",
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation failed: path={}, errors={}",
                request.getDescription(false).replace("uri=", ""),
                fieldErrors);

        ResponseErrorDTO errorResponse = ResponseErrorDTO.of(Code.VALIDATION_ERROR, "Validation failed");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseErrorDTO> handleGlobalException(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected exception occurred", ex);
        ResponseErrorDTO errorResponse = ResponseErrorDTO.of(Code.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Code mapCode(String code) {
        if (code == null || code.isBlank()) {
            return Code.BAD_REQUEST;
        }

        return switch (code) {
            case "RESOURCE_NOT_FOUND" -> Code.NOT_FOUND;
            case "VALIDATION_ERROR" -> Code.VALIDATION_ERROR;
            case "UNAUTHORIZED" -> Code.UNAUTHORIZED;
            case "FORBIDDEN" -> Code.FORBIDDEN;
            case "CONFLICT" -> Code.CONFLICT;
            case "INTERNAL_SERVER_ERROR" -> Code.INTERNAL_SERVER_ERROR;
            case "SERVER_DOWN" -> Code.SERVER_DOWN;
            default -> Code.BAD_REQUEST;
        };
    }
}
