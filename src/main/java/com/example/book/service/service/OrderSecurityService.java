package com.example.book.service.service;

import com.example.book.service.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Provides fine-grained, instance-level security checks for {@code Order} entities.
 * <p>
 * Similar to {@code ClientSecurityService}, this service is intended for use within
 * Spring Expression Language (SpEL) in {@code @PreAuthorize} annotations. Its primary
 * function is to determine if the currently authenticated user is the legitimate owner
 of a specific order they are attempting to access or modify. This prevents one
 * client from viewing or manipulating another client's orders.
 * <p>
 * The bean is registered with the name "orderSecurityService" for easy reference in SpEL.
 * Example usage in another service:
 * <pre>
 *     {@code @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")}
 *     public void cancelOrder(Long orderId) { ... }
 * </pre>
 *
 * @see ClientSecurityService
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Service("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    /**
     * Verifies if the currently authenticated user is the owner of the order
     * specified by the given {@code orderId}.
     * <p>
     * This method is essential for protecting order data. It works by finding the order
     * in the database and comparing the email of the associated client with the email
     * of the currently authenticated user.
     *
     * @param authentication The {@link Authentication} object, automatically provided by Spring Security.
     *                       It holds the identity of the currently logged-in user.
     * @param orderId        The unique identifier (ID) of the order being accessed. This value
     *                       is typically passed from a controller method's arguments via SpEL (e.g., {@code #orderId}).
     * @return {@code true} if the email of the authenticated user matches the email of the client
     *         who placed the order. Returns {@code false} if the order does not exist or if the
     *         emails do not match.
     */
    public boolean isOrderOwner(Authentication authentication, Long orderId) {
        // Get the email of the currently logged-in user.
        String currentUserEmail = authentication.getName();

        // Find the order by its ID.
        return orderRepository.findById(orderId)
                // If the order is found, navigate to its associated Client and get their email,
                // then compare it with the current user's email.
                .map(order -> order.getClient().getEmail().equals(currentUserEmail))
                // If no order is found for the given ID, deny access by defaulting to false.
                .orElse(false);
    }
}