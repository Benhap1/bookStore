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
 * Unit tests for OrderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Book book;

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

    @Nested
    @DisplayName("Tests for addBookToDraftOrder")
    class AddBookToDraftOrderTests {

        @Test
        @DisplayName("Should create a new order if no draft exists")
        void whenNoDraftOrder_shouldCreateNewOrder() {

            when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should add a new BookItem if the book is added to the draft for the first time")
        void whenBookIsNotInDraft_shouldAddNewBookItem() {

            Order draftOrder = new Order();
            draftOrder.setId(1L);
            draftOrder.setClient(client);
            draftOrder.setBookItems(new ArrayList<>());
            draftOrder.setStatus(OrderStatus.DRAFT);

            when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(orderRepository.findByClientIdAndStatus(client.getId(), OrderStatus.DRAFT)).thenReturn(Optional.of(draftOrder));

            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            assertEquals(1, draftOrder.getBookItems().size());
            assertEquals(1, draftOrder.getBookItems().get(0).getQuantity());
            assertEquals(0, new BigDecimal("20.00").compareTo(draftOrder.getPrice()));
            verify(orderRepository, times(1)).save(draftOrder);
        }

        @Test
        @DisplayName("Should increase quantity if the book is already in the draft")
        void whenBookIsInDraft_shouldIncrementQuantity() {

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

            orderService.addBookToDraftOrder(book.getId(), client.getEmail());

            assertEquals(3, existingItem.getQuantity());
            assertEquals(0, new BigDecimal("60.00").compareTo(draftOrder.getPrice()));
            verify(orderRepository, times(1)).save(draftOrder);
        }
    }

    @Nested
    @DisplayName("Tests for submitOrder")
    class SubmitOrderTests {

        @Test
        @DisplayName("Should successfully submit order if balance is sufficient")
        void whenBalanceIsSufficient_shouldSubmitOrderAndDeductBalance() {
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

            OrderDTO result = orderService.submitOrder(draftOrder.getId());

            assertNotNull(result);
            assertEquals(OrderStatus.SUBMITTED, draftOrder.getStatus());
            assertEquals(0, new BigDecimal("50.00").compareTo(client.getBalance()));

            verify(orderRepository, times(1)).save(draftOrder);
            verify(orderMapper, times(1)).toDTO(draftOrder);
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException if balance is insufficient")
        void whenBalanceIsInSufficient_shouldThrowException() {
            Order draftOrder = Order.builder()
                    .id(1L)
                    .client(client)
                    .status(OrderStatus.DRAFT)
                    .price(new BigDecimal("150.00"))
                    .build();

            when(orderRepository.findById(draftOrder.getId())).thenReturn(Optional.of(draftOrder));

            assertThrows(InsufficientFundsException.class, () -> {
                orderService.submitOrder(draftOrder.getId());
            });

            assertEquals(OrderStatus.DRAFT, draftOrder.getStatus());
            assertEquals(0, new BigDecimal("100.00").compareTo(client.getBalance()));
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomBadRequestException if order is not in DRAFT status")
        void whenOrderIsNotDraft_shouldThrowException() {
            Order submittedOrder = Order.builder()
                    .id(1L)
                    .client(client)
                    .status(OrderStatus.SUBMITTED)
                    .price(new BigDecimal("50.00"))
                    .build();

            when(orderRepository.findById(submittedOrder.getId())).thenReturn(Optional.of(submittedOrder));
            assertThrows(CustomBadRequestException.class, () -> {
                orderService.submitOrder(submittedOrder.getId());
            });
        }
    }
}
