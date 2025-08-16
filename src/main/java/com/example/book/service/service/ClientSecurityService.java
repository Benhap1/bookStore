package com.example.book.service.service;

import com.example.book.service.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Provides fine-grained security checks related to client accounts, often referred to as
 * Object-Level Security or instance-level security.
 * <p>
 * This service is designed to be used within Spring Expression Language (SpEL) in
 * {@code @PreAuthorize} annotations. It allows for complex authorization decisions
 * that go beyond simple role-based checks. For example, it can verify if the currently
 * authenticated user is the actual owner of a resource they are trying to access.
 * <p>
 * The bean is registered with the name "clientSecurityService" to be easily referenced in SpEL expressions.
 * Example usage in another service:
 * <pre>
 *     {@code @PreAuthorize("hasRole('ADMIN') or @clientSecurityService.isAccountOwner(authentication, #clientId)")}
 *     public ClientDTO getClientById(Long clientId) { ... }
 * </pre>
 *
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Service("clientSecurityService")
@RequiredArgsConstructor
public class ClientSecurityService {

    private final ClientRepository clientRepository;

    /**
     * Verifies if the currently authenticated user is the owner of the client account
     * specified by the given ID.
     * <p>
     * This method is the cornerstone of protecting user-specific data, ensuring that
     * a user can only view or modify their own information, unless they have administrative privileges.
     *
     * @param authentication The {@link Authentication} object, automatically supplied by Spring Security's
     *                       expression handler. It contains the details of the currently logged-in user,
     *                       including their principal (username/email).
     * @param id             The unique identifier (ID) of the client account that is being accessed. This value
     *                       is typically passed from the controller method's arguments using SpEL (e.g., {@code #id}).
     * @return {@code true} if the email of the authenticated user matches the email of the client
     *         found by the provided {@code id}. Returns {@code false} if the client does not exist
     *         or if the emails do not match.
     */
    public boolean isAccountOwner(Authentication authentication, Long id) {
        // Retrieve the email of the currently logged-in user from the Authentication principal.
        String currentUserEmail = authentication.getName();

        // Find the client account in the database using the target ID.
        return clientRepository.findById(id)
                // If the client exists, map it to a boolean by comparing its email to the current user's email.
                .map(client -> client.getEmail().equals(currentUserEmail))
                // If the client with the given ID is not found, default to false (access denied).
                .orElse(false);
    }
}
