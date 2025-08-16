package com.example.book.service;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.exception.CustomBadRequestException;
import com.example.book.service.exception.InsufficientFundsException;
import com.example.book.service.mapper.OrderMapper;
import com.example.book.service.model.Book;
import com.example.book.service.model.BookItem;
import com.example.book.service.model.Client;
import com.example.book.service.model.Order;
import com.example.book.service.model.enums.OrderStatus;
import com.example.book.service.repo.BookRepository;
import com.example.book.service.repo.ClientRepository;
import com.example.book.service.repo.OrderRepository;
import com.example.book.service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link OrderServiceImpl} class.
 * <p>
 * This class uses Mockito to isolate the service layer from the persistence layer (repositories)
 * and the mapping layer (mappers). Each test focuses on a specific business logic scenario
 * within the {@code OrderService}.
 * <p>
 * Tests are organized into nested classes for better structure and readability, with each
 * nested class corresponding to a specific method in the service.
 *
 * @see OrderServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    // --- Mocks for external dependencies ---

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private OrderMapper orderMapper;

    /**
     * The instance of the service to be tested. Mocks will be injected into this instance.
     */
    @InjectMocks
    private OrderServiceImpl orderService;

    // --- Test Data ---

    private Client client;
    private Book book;

    /**
     * Sets up common test data before each test case.
     * This method initializes a standard {@link Client} and {@link Book} object
     * to be used across multiple tests, ensuring a consistent starting point.
     */
    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .email("test@example.com")
                .balance(new BigDecimal("100.00"))
                .build();

        book = Book.builder()
                .id(1L)
                .name("Test Book")
                .price(new BigDecimal("20.00"))
                .build();
    }

    /**
     * A nested test class for all scenarios related to the {@code addBookToDraftOrder} method.
     */
    @Nested
    @DisplayName("Tests for addBookToDraftOrder")
    class AddBookToDraftOrderTests {

        /**
         * Verifies that a new {@link Order} is created and saved when a client adds a book
         * to their cart for the very first time (i.e., no draft order exists yet).
         */
        @Test
        @DisplayName("Should create a new order if no draft exists")
        void whenNoDraftOrder_shouldCreateNewOrder() {
            // Arrange: Mock the dependencies to simulate the scenario where no draft order is found.
            when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: Call the service method.
            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            // Assert: Verify that the save method was called exactly once, indicating a new order was created.
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        /**
         * Verifies that a new {@link BookItem} is correctly added to an existing draft order
         * when the book is not already in the cart.
         */
        @Test
        @DisplayName("Should add a new BookItem if the book is added to the draft for the first time")
        void whenBookIsNotInDraft_shouldAddNewBookItem() {
            // Arrange: Create an existing empty draft order.
            Order draftOrder = new Order();
            draftOrder.setId(1L);
            draftOrder.setClient(client);
            draftOrder.setBookItems(new ArrayList<>());
            draftOrder.setStatus(OrderStatus.DRAFT);
            when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)).thenReturn(Optional.of(draftOrder));

            // Act: Call the service method.
            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            // Assert: Check that the order now contains one item with the correct quantity and total price.
            assertEquals(1, draftOrder.getBookItems().size());
            assertEquals(1, draftOrder.getBookItems().get(0).getQuantity());
            assertEquals(0, new BigDecimal("20.00").compareTo(draftOrder.getPrice()));
            verify(orderRepository, times(1)).save(draftOrder);
        }

        /**
         * Verifies that the quantity of an existing {@link BookItem} is incremented
         * when the same book is added to the cart again.
         */
        @Test
        @DisplayName("Should increase quantity if the book is already in the draft")
        void whenBookIsInDraft_shouldIncrementQuantity() {
            // Arrange: Create a draft order that already contains the book.
            Order draftOrder = new Order();
            draftOrder.setId(1L);
            draftOrder.setClient(client);
            draftOrder.setStatus(OrderStatus.DRAFT);
            BookItem existingItem = new BookItem();
            existingItem.setBook(book);
            existingItem.setQuantity(2);
            existingItem.setOrder(draftOrder);
            draftOrder.setBookItems(new ArrayList<>(java.util.List.of(existingItem)));
            when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)).thenReturn(Optional.of(draftOrder));

            // Act: Call the service method.
            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            // Assert: Check that the quantity is now 3 and the total price is recalculated correctly.
            assertEquals(3, existingItem.getQuantity());
            assertEquals(0, new BigDecimal("60.00").compareTo(draftOrder.getPrice()));
            verify(orderRepository, times(1)).save(draftOrder);
        }
    }

    /**
     * A nested test class for all scenarios related to the {@code submitOrder} method.
     */
    @Nested
    @DisplayName("Tests for submitOrder")
    class SubmitOrderTests {

        /**
         * Verifies that an order is successfully submitted when the client's balance
         * is sufficient to cover the order price. The balance should be deducted.
         */
        @Test
        @DisplayName("Should successfully submit order if balance is sufficient")
        void whenBalanceIsSufficient_shouldSubmitOrderAndDeductBalance() {
            // Arrange: Create a draft order with a price less than the client's balance.
            Order draftOrder = Order.builder()
                    .id(1L)
                    .client(client)
                    .status(OrderStatus.DRAFT)
                    .price(new BigDecimal("50.00"))
                    .build();
            OrderDTO expectedDTO = new OrderDTO();
            when(orderRepository.findById(draftOrder.getId())).thenReturn(Optional.of(draftOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(orderMapper.toDTO(draftOrder)).thenReturn(expectedDTO);

            // Act: Call the service method.
            OrderDTO result = orderService.submitOrder(draftOrder.getId());

            // Assert: Check that the order status is updated, balance is deducted, and the DTO is returned.
            assertNotNull(result);
            assertEquals(OrderStatus.SUBMITTED, draftOrder.getStatus());
            assertEquals(0, new BigDecimal("50.00").compareTo(client.getBalance()));
            verify(orderRepository, times(1)).save(draftOrder);
            verify(orderMapper, times(1)).toDTO(draftOrder);
        }

        /**
         * Verifies that an {@link InsufficientFundsException} is thrown when a client
         * attempts to submit an order with a total price exceeding their current balance.
         */
        @Test
        @DisplayName("Should throw InsufficientFundsException if balance is insufficient")
        void whenBalanceIsInSufficient_shouldThrowException() {
            // Arrange: Create a draft order with a price greater than the client's balance.
            Order draftOrder = Order.builder()
                    .id(1L)
                    .client(client)
                    .status(OrderStatus.DRAFT)
                    .price(new BigDecimal("150.00"))
                    .build();
            when(orderRepository.findById(draftOrder.getId())).thenReturn(Optional.of(draftOrder));

            // Act & Assert: Expect the specific exception to be thrown.
            assertThrows(InsufficientFundsException.class, () -> orderService.submitOrder(draftOrder.getId()));

            // Assert (Post-condition): Ensure no state changes occurred (order is still a draft, balance is unchanged).
            assertEquals(OrderStatus.DRAFT, draftOrder.getStatus());
            assertEquals(0, new BigDecimal("100.00").compareTo(client.getBalance()));
            verify(orderRepository, never()).save(any());
        }

        /**
         * Verifies that a {@link CustomBadRequestException} is thrown when attempting
         * to submit an order that is not in the DRAFT status.
         */
        @Test
        @DisplayName("Should throw CustomBadRequestException if order is not in DRAFT status")
        void whenOrderIsNotDraft_shouldThrowException() {
            // Arrange: Create an order that is already submitted.
            Order submittedOrder = Order.builder()
                    .id(1L)
                    .client(client)
                    .status(OrderStatus.SUBMITTED)
                    .price(new BigDecimal("50.00"))
                    .build();
            when(orderRepository.findById(submittedOrder.getId())).thenReturn(Optional.of(submittedOrder));

            // Act & Assert: Expect the specific exception to be thrown.
            assertThrows(CustomBadRequestException.class, () -> orderService.submitOrder(submittedOrder.getId()));
        }
    }
}