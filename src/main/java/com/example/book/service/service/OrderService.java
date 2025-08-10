package com.example.book.service.service;

import com.example.book.service.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getOrdersByClient(String clientEmail);

    OrderDTO submitOrder(Long orderId);

    OrderDTO confirmOrder(Long orderId);

    OrderDTO cancelOrder(Long orderId);

    List<OrderDTO> getAllOrders();

    void addBookToDraftOrder(Long bookId, String clientEmail);

    List<OrderDTO> searchOrdersByClientEmail(String email);
}
