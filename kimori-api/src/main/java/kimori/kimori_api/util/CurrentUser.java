package kimori.kimori_api.util;

import org.springframework.security.core.context.SecurityContextHolder;

import kimori.kimori_api.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Resolves the authenticated Firebase UID for the current request (FR-022a). */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static String uid() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Sign-in required");
        }
        return authentication.getPrincipal().toString();
    }
}
