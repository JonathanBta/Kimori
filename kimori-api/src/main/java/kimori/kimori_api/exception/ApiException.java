package kimori.kimori_api.exception;

import org.springframework.http.HttpStatus;

/** Thrown for client-facing errors (validation, not-found, forbidden) with a stable error code. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String field;

    public ApiException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }

    public ApiException(HttpStatus status, String errorCode, String message, String field) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.field = field;
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public static ApiException validation(String message, String field) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, field);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }
}
