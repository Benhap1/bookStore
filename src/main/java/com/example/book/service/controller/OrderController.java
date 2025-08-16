package com.example.book.service.controller;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.InsufficientFundsException;
import com.example.book.service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;




/**
 * Controller for handling all web requests related to orders and the shopping cart.
 * <p>
 * This controller manages the full lifecycle of an order from the user's perspective,
 * separating logic based on roles:
 * <ul>
 *   <li><b>Clients (ROLE_CLIENT):</b> Can view their own orders, manage their shopping cart
 *   (which is an order in a 'DRAFT' state), and submit orders for processing.</li>
 *   <li><b>Admins (ROLE_ADMIN):</b> Can view, search, confirm, and cancel all orders
 *   in the system.</li>
 * </ul>
 * Security is enforced at the method level using {@code @PreAuthorize} annotations.
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Displays the "My Orders" page for the currently authenticated client.
     * <p>
     * This method fetches the client's orders and splits them into two lists:
     * one for the current shopping cart (draft orders) and one for the order history.
     * It is accessible only to users with the 'CLIENT' role.
     *
     * @param userDetails The currently authenticated user, injected by Spring Security.
     * @param model       The {@link Model} to which the order lists are added.
     * @return The view name for the client's order page ("orders/list").
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public String getCurrentClientOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            // Assuming OrderService provides methods to get orders by status
            model.addAttribute("draftOrders", orderService.getDraftOrdersByClient(email));
            model.addAttribute("completedOrders", orderService.getCompletedOrdersByClient(email));
        }
        return "orders/list";
    }

    /**
     * Displays the order management page for administrators.
     * <p>
     * This method is accessible only to users with the 'ADMIN' role. It displays all
     * orders and includes a search functionality to filter orders by the client's email.
     *
     * @param email An optional request parameter to filter orders by client email.
     * @param model The {@link Model} to which the list of orders and search term are added.
     * @return The view name for the admin order management page ("orders/admin-list").
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageAllOrders(@RequestParam(value = "email", required = false) String email, Model model) {
        List<OrderDTO> orders;
        if (email != null && !email.isBlank()) {
            orders = orderService.searchOrdersByClientEmail(email);
        } else {
            orders = orderService.getAllOrders();
        }
        model.addAttribute("orders", orders);
        model.addAttribute("email", email);
        return "orders/admin-list";
    }

    /**
     * Handles the action for an admin to confirm a submitted order.
     *
     * @param orderId            The ID of the order to confirm.
     * @param redirectAttributes Used to add a success flash message.
     * @return A redirect to the admin order management page.
     */
    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public String confirmOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        orderService.confirmOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been confirmed.");
        return "redirect:/orders/all";
    }

    /**
     * Handles the action to cancel an order.
     * <p>
     * This method can be invoked by an admin for any order, or by a client for their
     * own order, as enforced by the {@code @PreAuthorize} annotation using a custom
     * security service method {@code @orderSecurityService.isOrderOwner}.
     * It redirects the user back to the appropriate page after the action.
     *
     * @param orderId            The ID of the order to cancel.
     * @param returnUrl          A parameter indicating where to redirect the user after cancellation.
     * @param redirectAttributes Used to add a success flash message.
     * @return A redirect string based on the provided {@code returnUrl}.
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(defaultValue = "/orders/my") String returnUrl,
                              RedirectAttributes redirectAttributes) {
        orderService.cancelOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been cancelled.");
        return "redirect:" + returnUrl;
    }

    /**
     * Handles a client's request to add a book to their shopping cart.
     *
     * @param bookId             The ID of the book to add.
     * @param userDetails        The currently authenticated client.
     * @param redirectAttributes Used to add a success flash message.
     * @return A redirect to the main book list page.
     */
    @PostMapping("/cart/add/{bookId}")
    @PreAuthorize("hasRole('CLIENT')")
    public String addBookToCart(@PathVariable Long bookId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        orderService.addBookToDraftOrder(bookId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Book added to your cart!");
        return "redirect:/books/list";
    }

    /**
     * Handles the submission of a client's shopping cart (draft order).
     * <p>
     * This method attempts to submit the order via the {@link OrderService}. It includes
     * error handling to catch specific business exceptions, such as {@link InsufficientFundsException},
     * and provides appropriate user feedback.
     *
     * @param orderId            The ID of the draft order to submit.
     * @param redirectAttributes Used to add success or error flash messages.
     * @return A redirect to the client's "My Orders" page.
     */
    @PostMapping("/cart/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public String submitCartOrder(@RequestParam Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.submitOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Your order has been submitted successfully!");
        } catch (InsufficientFundsException e) {
            // Provide specific feedback if the client's balance is too low.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            // Provide a generic error message for any other unexpected issues.
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while submitting the order.");
        }
        return "redirect:/orders/my";
    }
}