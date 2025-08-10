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


@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public String viewMyProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("client", clientService.getClientByEmail(userDetails.getUsername()));
        return "clients/profile";
    }

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
        model.addAttribute("email", email);
        return "clients/list";
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public String blockClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clientService.blockClient(id);
        redirectAttributes.addFlashAttribute("successMessage", "Client has been blocked.");
        return "redirect:/clients/list";
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public String unblockClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clientService.unblockClient(id);
        redirectAttributes.addFlashAttribute("successMessage", "Client has been unblocked.");
        return "redirect:/clients/list";
    }

    @PostMapping("/profile/topup")
    @PreAuthorize("hasRole('CLIENT')")
    public String handleTopUpBalance(@RequestParam("amount") BigDecimal amount,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            clientService.topUpBalance(userDetails.getUsername(), amount);
            redirectAttributes.addFlashAttribute("successMessage", "Your balance has been successfully topped up by $" + amount + "!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        }
        return "redirect:/clients/profile";
    }
}