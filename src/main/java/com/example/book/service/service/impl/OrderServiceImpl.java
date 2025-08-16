package com.example.book.service.service.impl;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.CustomBadRequestException;
import com.example.book.service.exception.InsufficientFundsException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.OrderMapper;
import com.example.book.service.model.*;
import com.example.book.service.model.enums.OrderStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link OrderService} interface.
 * <p>
 * This class orchestrates all business logic related to the order lifecycle,
 * from shopping cart management (draft orders) to submission, confirmation,
 * and cancellation. It coordinates interactions between {@link Order},
 * {@link Client}, and {@link Book} entities and their respective repositories.
 * <p>
 * All methods are secured with {@code @PreAuthorize} annotations to enforce
 * role-based and ownership-based access control. All operations that modify
 * the database are marked as {@code @Transactional} to ensure data integrity.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public List<OrderDTO> getDraftOrdersByClient(String clientEmail) {
        // Assuming OrderRepository has a method to find by client email and status
        return orderRepository.findAllByClientEmailAndStatus(clientEmail, OrderStatus.DRAFT).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLIENT')")
    public List<OrderDTO> getCompletedOrdersByClient(String clientEmail) {
        // Assuming OrderRepository has a method to find orders with a status other than DRAFT
        return orderRepository.findAllByClientEmailAndStatusNot(clientEmail, OrderStatus.DRAFT).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation contains critical business logic:
     * 1. Validates that the order is in 'DRAFT' status.
     * 2. Checks if the client has sufficient balance to cover the order price.
     * 3. Deducts the order price from the client's balance.
     * 4. Updates the order status to 'SUBMITTED'.
     * The entire operation is transactional.
     */
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

        // Business rule: Check for sufficient funds before proceeding.
        if (client.getBalance().compareTo(orderPrice) < 0) {
            BigDecimal shortfall = orderPrice.subtract(client.getBalance());
            throw new InsufficientFundsException(
                    "Insufficient funds to submit the order. Your balance is $" + client.getBalance() +
                            ", but the order total is $" + orderPrice + ". Please top up your balance by at least $" + shortfall + "."
            );
        }

        // Deduct funds and update status in a single transaction.
        client.setBalance(client.getBalance().subtract(orderPrice));
        order.setStatus(OrderStatus.SUBMITTED);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDTO confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Business rule: Only submitted orders can be confirmed.
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new CustomBadRequestException("Only orders in SUBMITTED status can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwner(authentication, #orderId)")
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Business rule: Only submitted or confirmed orders can be cancelled.
        if (order.getStatus() != OrderStatus.SUBMITTED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new CustomBadRequestException("Only orders in SUBMITTED or CONFIRMED status can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"))
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method implements the core shopping cart logic. It finds the client's
     * active draft order or creates a new one if none exists. It then adds the
     * specified book to the order, incrementing the quantity if the book is already
     * present. Finally, it recalculates and updates the total price of the order.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public void addBookToDraftOrder(Long bookId, String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + bookId));

        // Find an existing draft order or create a new one on the fly.
        Order draftOrder = orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setClient(client);
                    newOrder.setOrderDate(LocalDateTime.now());
                    newOrder.setStatus(OrderStatus.DRAFT);
                    newOrder.setPrice(BigDecimal.ZERO);
                    return newOrder;
                });

        // Check if the book is already in the cart to avoid duplicate line items.
        Optional<BookItem> existingItem = draftOrder.getBookItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            // If it exists, just increment the quantity.
            BookItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Otherwise, create a new line item.
            BookItem newItem = new BookItem();
            newItem.setOrder(draftOrder);
            newItem.setBook(book);
            newItem.setQuantity(1);
            draftOrder.getBookItems().add(newItem);
        }

        // Recalculate the total price based on all items in the cart.
        BigDecimal totalPrice = draftOrder.getBookItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        draftOrder.setPrice(totalPrice);

        // Save the order, which will also cascade-save any new or updated BookItems.
        orderRepository.save(draftOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> searchOrdersByClientEmail(String email) {
        return orderRepository.findOrdersByClientEmail(email).stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }
}