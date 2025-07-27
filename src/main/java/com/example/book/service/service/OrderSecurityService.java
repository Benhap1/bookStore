package com.example.book.service.service;

import com.example.book.service.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    public boolean isOrderOwner(Authentication authentication, Long orderId) {
        String currentUserEmail = authentication.getName();
        return orderRepository.findById(orderId)
                .map(order -> order.getClient().getEmail().equals(currentUserEmail))
                .orElse(false);
    }
}
