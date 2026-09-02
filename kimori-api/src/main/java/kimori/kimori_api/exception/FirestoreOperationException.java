package kimori.kimori_api.exception;

/** Wraps checked Firestore SDK exceptions (InterruptedException/ExecutionException) as unchecked. */
public class FirestoreOperationException extends RuntimeException {
    public FirestoreOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
