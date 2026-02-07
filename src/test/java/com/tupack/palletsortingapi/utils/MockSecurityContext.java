package com.tupack.palletsortingapi.utils;

import com.tupack.palletsortingapi.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for mocking Spring Security context in tests.
 * Provides methods to set and clear authenticated users.
 */
public class MockSecurityContext {

    /**
     * Sets an authenticated user in the Security Context
     *
     * @param user User to set as authenticated
     */
    public static void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Clears the Security Context (useful for test cleanup)
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
