package muse.back.service.common.exception;

import lombok.Getter;

@Getter
public class MuseException extends RuntimeException {
    private final String code;
    private final int status;

    public MuseException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public MuseException(String code, String message) {
        super(message);
        this.code = code;
        this.status = 400;
    }

    // 자주 사용되는 예외들
    public static class ResourceNotFoundException extends MuseException {
        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(
                    "RESOURCE_NOT_FOUND",
                    String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                    404
            );
        }
    }

    public static class ValidationException extends MuseException {
        public ValidationException(String message) {
            super("VALIDATION_ERROR", message, 400);
        }
    }

    public static class UnauthorizedException extends MuseException {
        public UnauthorizedException(String message) {
            super("UNAUTHORIZED", message, 401);
        }
    }

    public static class ForbiddenException extends MuseException {
        public ForbiddenException(String message) {
            super("FORBIDDEN", message, 403);
        }
    }

    public static class ConflictException extends MuseException {
        public ConflictException(String message) {
            super("CONFLICT", message, 409);
        }
    }
}
