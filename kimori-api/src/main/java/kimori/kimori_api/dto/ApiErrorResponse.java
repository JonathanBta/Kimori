package kimori.kimori_api.dto;

/** Uniform error shape returned for all 4xx failures, per contracts/api.md. */
public record ApiErrorResponse(int status, String error, String message, String field) {
    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, null);
    }
}
