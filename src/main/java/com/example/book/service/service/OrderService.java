package com.example.book.service.service;

import com.example.book.service.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getOrdersByClient(String clientEmail);

    OrderDTO addOrder(OrderDTO order);

    OrderDTO submitOrder(Long orderId);

    OrderDTO confirmOrder(Long orderId);

    OrderDTO cancelOrder(Long orderId);
}
