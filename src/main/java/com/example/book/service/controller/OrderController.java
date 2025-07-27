package com.example.book.service.controller;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getCurrentClientOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getOrdersByClient(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }


    @PostMapping
    public ResponseEntity<OrderDTO> addOrder(@RequestBody @Valid OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.addOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PatchMapping("/{orderId}/submit")
    public ResponseEntity<OrderDTO> submitOrder(@PathVariable Long orderId) {
        OrderDTO submittedOrder = orderService.submitOrder(orderId);
        return ResponseEntity.ok(submittedOrder);
    }

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long orderId) {
        OrderDTO confirmedOrder = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(confirmedOrder);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        OrderDTO cancelledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }
}
