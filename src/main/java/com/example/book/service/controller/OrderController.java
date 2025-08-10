package com.example.book.service.controller;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.InsufficientFundsException;
import com.example.book.service.model.enums.OrderStatus;
import com.example.book.service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.stream.Collectors;



@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

//    @GetMapping("/my")
//    @PreAuthorize("hasRole('CLIENT')")
//    public String getCurrentClientOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        if (userDetails != null) {
//            List<OrderDTO> allOrders = orderService.getOrdersByClient(userDetails.getUsername());
//            List<OrderDTO> draftOrders = allOrders.stream()
//                    .filter(o -> o.getStatus() == OrderStatus.DRAFT)
//                    .collect(Collectors.toList());
//
//            List<OrderDTO> completedOrders = allOrders.stream()
//                    .filter(o -> o.getStatus() != OrderStatus.DRAFT)
//                    .collect(Collectors.toList());
//            model.addAttribute("draftOrders", draftOrders);
//            model.addAttribute("completedOrders", completedOrders);
//        }
//        return "orders/list";
//    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public String getCurrentClientOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            model.addAttribute("draftOrders", orderService.getDraftOrdersByClient(email));
            model.addAttribute("completedOrders", orderService.getCompletedOrdersByClient(email));
        }
        return "orders/list";
    }


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

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public String confirmOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        orderService.confirmOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been confirmed.");
        return "redirect:/orders/all";
    }

//    @PostMapping("/{orderId}/cancel")
//    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")
//    public String cancelOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
//        orderService.cancelOrder(orderId);
//        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been cancelled.");
//        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
//            return "redirect:/orders/all";
//        }
//        return "redirect:/orders/my";
//    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(defaultValue = "/orders/my") String returnUrl,
                              RedirectAttributes redirectAttributes) {
        orderService.cancelOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been cancelled.");
        return "redirect:" + returnUrl;
    }


    @PostMapping("/cart/add/{bookId}")
    @PreAuthorize("hasRole('CLIENT')")
    public String addBookToCart(@PathVariable Long bookId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        orderService.addBookToDraftOrder(bookId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Book added to your cart!");
        return "redirect:/books/list";
    }


    @PostMapping("/cart/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public String submitCartOrder(@RequestParam Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.submitOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Your order has been submitted successfully!");
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while submitting the order.");
        }
        return "redirect:/orders/my";
    }
}