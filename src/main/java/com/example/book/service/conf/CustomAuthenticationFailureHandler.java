package com.example.book.service.conf;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A custom handler for managing authentication failures in Spring Security.
 * <p>
 * Instead of relying on the default Spring Security behavior which shows a generic
 * error for all login failures, this handler provides more specific user feedback.
 * It inspects the type of {@link AuthenticationException} thrown during the login process
 * and redirects the user to different URLs based on the failure reason. For instance,
 * it distinguishes between a generic bad credential error and a specific error for a
 * disabled account.
 *
 * @see AuthenticationFailureHandler
 * @see SecurityConfig // This handler is typically configured in the main SecurityConfig.
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Invoked when a user authentication attempt fails.
     * <p>
     * This implementation checks the specific type of the {@code AuthenticationException}.
     * If the exception is a {@link DisabledException}, it redirects the user to a URL
     * indicating that their account is blocked. For all other authentication errors,
     * it redirects to a generic error URL.
     *
     * @param request   the {@code HttpServletRequest} where the authentication attempt occurred.
     * @param response  the {@code HttpServletResponse} to send the redirect to.
     * @param exception the {@code AuthenticationException} that was thrown, containing the reason for the failure.
     * @throws IOException      if an input or output error occurs during the redirect.
     * @throws ServletException if a servlet-specific error occurs.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Default redirect URL for generic "bad credentials" errors.
        String redirectUrl = "/auth/login?error=true";

        // Check if the failure was specifically due to the user account being disabled.
        if (exception instanceof DisabledException) {
            // If so, change the redirect URL to one that indicates a disabled account.
            redirectUrl = "/auth/login?disabled=true";
        }

        // This is extensible. In the future, you could check for other exception types:
        // else if (exception instanceof LockedException) {
        //     redirectUrl = "/auth/login?locked=true";
        // }

        // Perform the redirect to the determined URL.
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}