package com.example.book.service.service.impl;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.CustomBadRequestException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.OrderMapper;
import com.example.book.service.model.*;
import com.example.book.service.model.enums.OrderStatus;
import com.example.book.service.repo.BookRepository;
import com.example.book.service.repo.ClientRepository;
import com.example.book.service.repo.OrderRepository;
import com.example.book.service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));

        return orderRepository.findAllByClient(client)
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(() -> new NotFoundException("Client not found: " + orderDTO.getClientEmail()));

        if (orderDTO.getBookItems() == null || orderDTO.getBookItems().isEmpty()) {
            throw new CustomBadRequestException("Order must contain at least one book item");
        }

        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.DRAFT);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<BookItem> bookItems = new ArrayList<>();

        for (BookItemDTO itemDTO : orderDTO.getBookItems()) {
            Long bookId = itemDTO.getId();
            if (bookId == null) {
                throw new CustomBadRequestException("Book ID must be provided for each order item.");
            }

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new NotFoundException("Book not found with id: " + bookId));

            BookItem bookItem = new BookItem();
            bookItem.setOrder(order);
            bookItem.setBook(book);
            bookItem.setQuantity(itemDTO.getQuantity());

            bookItems.add(bookItem);

            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        order.setPrice(totalPrice);
        order.setBookItems(bookItems);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toDTO(savedOrder);
    }

    @Override
    @PreAuthorize("hasRole('CLIENT') and @orderSecurityService.isOrderOwner(authentication, #orderId)")
    public OrderDTO submitOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomBadRequestException("Only orders in DRAFT status can be submitted");
        }

        order.setStatus(OrderStatus.SUBMITTED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDTO confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new CustomBadRequestException("Only orders in SUBMITTED status can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.SUBMITTED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new CustomBadRequestException("Only orders in SUBMITTED or CONFIRMED status can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }
}