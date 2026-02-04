package com.kerem.phinance.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations. Provides methods to safely
 * extract authenticated user information from the security context.
 */
@Component
public class SecurityUtils {

    /**
     * Get the currently authenticated user's ID from the security context. This
     * ensures that the user ID cannot be manipulated from client requests.
     *
     * @return the authenticated user's ID
     * @throws IllegalStateException if no user is authenticated or the
     * principal is not a UserPrincipal
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException("Invalid principal type");
        }

        return ((UserPrincipal) principal).getId();
    }

    /**
     * Get the currently authenticated UserPrincipal.
     *
     * @return the authenticated UserPrincipal
     * @throws IllegalStateException if no user is authenticated or the
     * principal is not a UserPrincipal
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException("Invalid principal type");
        }

        return (UserPrincipal) principal;
    }

    /**
     * Get the currently authenticated user's email.
     *
     * @return the authenticated user's email
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserPrincipal().getEmail();
    }
}
