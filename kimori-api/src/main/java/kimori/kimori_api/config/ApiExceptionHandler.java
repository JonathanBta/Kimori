package kimori.kimori_api.config;

import kimori.kimori_api.dto.ApiErrorResponse;
import kimori.kimori_api.exception.ApiException;
import kimori.kimori_api.exception.FirestoreOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiErrorResponse(ex.getStatus().value(), ex.getErrorCode(), ex.getMessage(), ex.getField()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().isEmpty()
                ? null
                : ex.getBindingResult().getFieldErrors().get(0).getField();
        String message = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Validation failed"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(400, "VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(400, "VALIDATION_ERROR", "Uploaded file exceeds the 10MB limit", "file"));
    }

    @ExceptionHandler(FirestoreOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleFirestore(FirestoreOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(500, "STORAGE_ERROR", "A data storage error occurred", null));
    }
}
