package com.example.book.service.service.impl;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.CustomBadRequestException;
import com.example.book.service.exception.InsufficientFundsException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.OrderMapper;
import com.example.book.service.model.*;
import com.example.book.service.model.enums.OrderStatus;
import com.example.book.service.repo.BookItemRepository;
import com.example.book.service.repo.BookRepository;
import com.example.book.service.repo.ClientRepository;
import com.example.book.service.repo.OrderRepository;
import com.example.book.service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;
    private final BookItemRepository bookItemRepository;


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
    @PreAuthorize("hasRole('CLIENT') and @orderSecurityService.isOrderOwner(authentication, #orderId)")
    public OrderDTO submitOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomBadRequestException("Only orders in DRAFT status can be submitted");
        }

        Client client = order.getClient();
        BigDecimal orderPrice = order.getPrice();
        if (client.getBalance().compareTo(orderPrice) < 0) {
            BigDecimal shortfall = orderPrice.subtract(client.getBalance());
            throw new InsufficientFundsException(
                    "Insufficient funds to submit the order. Your balance is $" + client.getBalance() +
                            ", but the order total is $" + orderPrice + ". Please top up your balance by at least $" + shortfall + "."
            );
        }
        client.setBalance(client.getBalance().subtract(orderPrice));
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
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")
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

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"))
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void addBookToDraftOrder(Long bookId, String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + bookId));

        Order draftOrder = orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setClient(client);
                    newOrder.setOrderDate(LocalDateTime.now());
                    newOrder.setStatus(OrderStatus.DRAFT);
                    newOrder.setPrice(BigDecimal.ZERO);
                    return newOrder;
                });

        Optional<BookItem> existingItem = draftOrder.getBookItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            BookItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
        } else {
            BookItem newItem = new BookItem();
            newItem.setOrder(draftOrder);
            newItem.setBook(book);
            newItem.setQuantity(1);
            draftOrder.getBookItems().add(newItem);
        }

        BigDecimal totalPrice = draftOrder.getBookItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        draftOrder.setPrice(totalPrice);
        orderRepository.save(draftOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> searchOrdersByClientEmail(String email) {
        return orderRepository.findOrdersByClientEmail(email).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }
}