package com.example.book.service.controller;

import com.example.book.service.dto.ClientDTO;
import com.example.book.service.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;


/**
 * Controller for handling client-related web requests.
 * <p>
 * This controller serves two distinct user roles:
 * 1.  <b>Clients:</b> It provides endpoints for clients to view their own profile and
 *     manage their account balance.
 * 2.  <b>Admins:</b> It provides endpoints for administrators to view, search, and
 *     manage all client accounts in the system, including blocking and unblocking them.
 * <p>
 * Access to its methods is secured using {@code @PreAuthorize} annotations to ensure
 * that only users with the appropriate roles can perform these actions.
 */
@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Displays the profile page for the currently authenticated client.
     * <p>
     * This method is only accessible to users with the 'CLIENT' role. It retrieves the
     * client's details using their email (username) from the security context and
     * adds the client DTO to the model for rendering.
     *
     * @param userDetails The currently authenticated user's details, injected by Spring Security.
     * @param model       The {@link Model} to which the client data is added.
     * @return The view name for the client profile page ("clients/profile").
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewMyProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("client", clientService.getClientByEmail(userDetails.getUsername()));
        return "clients/profile";
    }

    /**
     * Displays a list of all clients for administrative purposes.
     * <p>
     * This method is only accessible to users with the 'ADMIN' role. It supports an
     * optional search functionality by email. If an email keyword is provided, it returns
     * a filtered list; otherwise, it returns all clients.
     *
     * @param email An optional request parameter to filter clients by their email address.
     * @param model The {@link Model} to which the list of clients and the search keyword are added.
     * @return The view name for the admin-facing client list ("clients/list").
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllClients(@RequestParam(value = "email", required = false) String email, Model model) {
        List<ClientDTO> clients;
        if (email != null && !email.isBlank()) {
            clients = clientService.searchClientsByEmail(email);
        } else {
            clients = clientService.getAllClients();
        }
        model.addAttribute("clients", clients);
        model.addAttribute("email", email); // Pass the keyword back to the view
        return "clients/list";
    }

    /**
     * Handles the action to block a specific client account.
     * <p>
     * This is POST endpoint accessible only to admins. It delegates the blocking
     * logic to the {@link ClientService} and redirects back to the client list
     * with a success message.
     *
     * @param id                 The ID of the client to block.
     * @param redirectAttributes Used to add a flash attribute for the success message.
     * @return A redirect string to the client management page.
     */
    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public String blockClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clientService.blockClient(id);
        redirectAttributes.addFlashAttribute("successMessage", "Client has been blocked.");
        return "redirect:/clients/list";
    }

    /**
     * Handles the action to unblock a specific client account.
     * <p>
     * This is POST endpoint accessible only to admins. It delegates the unblocking
     * logic to the {@link ClientService} and redirects back to the client list
     * with a success message.
     *
     * @param id                 The ID of the client to unblock.
     * @param redirectAttributes Used to add a flash attribute for the success message.
     * @return A redirect string to the client management page.
     */
    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public String unblockClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clientService.unblockClient(id);
        redirectAttributes.addFlashAttribute("successMessage", "Client has been unblocked.");
        return "redirect:/clients/list";
    }

    /**
     * Handles the form submission for a client to top up their account balance.
     * <p>
     * This endpoint is accessible only to the authenticated clients. It validates the
     * amount, delegates the balance update logic to the {@link ClientService}, and handles
     * potential errors (e.g., a non-positive amount). It then redirects back to the
     * profile page with a success or error message.
     *
     * @param amount             The amount to add to the balance, from the form.
     * @param userDetails        The currently authenticated user's details, for security.
     * @param redirectAttributes Used to add flash attributes for success or error messages.
     * @return A redirect string to the client's profile page.
     */
    @PostMapping("/profile/topup")
    @PreAuthorize("hasRole('CLIENT')")
    public String handleTopUpBalance(@RequestParam("amount") BigDecimal amount,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            clientService.topUpBalance(userDetails.getUsername(), amount);
            redirectAttributes.addFlashAttribute("successMessage", "Your balance has been successfully topped up by $" + amount + "!");
        } catch (IllegalArgumentException e) {
            // Catches specific, expected errors like a negative amount.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // Catches any other unexpected errors.
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        }
        return "redirect:/clients/profile";
    }
}