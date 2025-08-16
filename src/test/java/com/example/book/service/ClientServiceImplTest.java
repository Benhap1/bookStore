package com.example.book.service;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.ClientMapper;
import com.example.book.service.model.Client;
import com.example.book.service.model.enums.Role;
import com.example.book.service.repo.ClientRepository;
import com.example.book.service.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link ClientServiceImpl} class.
 * <p>
 * This class validates the core business logic related to client management,
 * including creation, retrieval, status changes, and balance updates. Mockito is used
 * to isolate the service from the database and other dependencies, ensuring tests
 * are fast and focused on the service's logic.
 *
 * @see ClientServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    /**
     * Mock of the {@link ClientRepository} to simulate database operations for clients.
     */
    @Mock
    private ClientRepository clientRepository;

    /**
     * Mock of the {@link ClientMapper} to simulate mapping between entities and DTOs.
     */
    @Mock
    private ClientMapper clientMapper;

    /**
     * Mock of the {@link PasswordEncoder} to simulate password hashing without
     * needing the actual encoding algorithm.
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * The instance of {@link ClientServiceImpl} being tested, with mocks injected.
     */
    @InjectMocks
    private ClientServiceImpl clientService;

    /**
     * Tests the successful creation of a new client.
     * It verifies that when a unique email is provided, the service correctly
     * encodes the password, sets default properties (role, enabled status),
     * saves the new client, and returns the corresponding DTO.
     */
    @Test
    @DisplayName("addClient: should successfully create a client if the email is unique")
    void whenAddClient_withUniqueEmail_thenCreateClient() {
        // Arrange
        ClientCreateRequestDTO createDTO = ClientCreateRequestDTO.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        Client clientToSave = new Client();
        Client savedClient = Client.builder().id(1L).email("new@example.com").build();
        ClientDTO expectedDTO = ClientDTO.builder().id(1L).email("new@example.com").build();

        when(clientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(clientMapper.toEntity(createDTO)).thenReturn(clientToSave);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        when(clientMapper.toDTO(savedClient)).thenReturn(expectedDTO);

        // Act
        ClientDTO result = clientService.addClient(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("new@example.com", result.getEmail());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(clientMapper).toEntity(createDTO);
        verify(clientRepository, times(1)).save(clientToSave);

        // Verify that default properties were set correctly on the entity before saving.
        assertEquals(Role.CLIENT, clientToSave.getRole());
        assertTrue(clientToSave.isEnabled());
    }

    /**
     * Tests the business rule that prevents creating clients with duplicate emails.
     * It verifies that if the repository finds an existing client with the same email,
     * an {@link AlreadyExistException} is thrown and no save operation is attempted.
     */
    @Test
    @DisplayName("addClient: should throw AlreadyExistException if the email already exists")
    void whenAddClient_withExistingEmail_thenThrowAlreadyExistException() {
        // Arrange
        ClientCreateRequestDTO createDTO = ClientCreateRequestDTO.builder().email("existing@example.com").build();
        Client existingClient = new Client();

        when(clientRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingClient));

        // Act & Assert
        assertThrows(AlreadyExistException.class, () -> clientService.addClient(createDTO));

        verify(passwordEncoder, never()).encode(anyString());
        verify(clientRepository, never()).save(any());
    }

    /**
     * Tests the successful retrieval of a client by email.
     * It verifies that if a client is found, it is correctly mapped to a {@link ClientDTO} and returned.
     */
    @Test
    @DisplayName("getClientByEmail: should return ClientDTO when the client exists")
    void whenGetClientByEmail_whenExists_thenReturnClientDTO() {
        // Arrange
        String existingEmail = "found@example.com";
        Client client = Client.builder().id(1L).email(existingEmail).build();
        ClientDTO expectedDTO = ClientDTO.builder().id(1L).email(existingEmail).build();

        when(clientRepository.findByEmail(existingEmail)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(expectedDTO);

        // Act
        ClientDTO result = clientService.getClientByEmail(existingEmail);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.getId(), result.getId());
        assertEquals(expectedDTO.getEmail(), result.getEmail());

        verify(clientRepository, times(1)).findByEmail(existingEmail);
        verify(clientMapper, times(1)).toDTO(client);
    }

    /**
     * Tests the scenario where a requested client email does not exist.
     * It verifies that the service correctly throws a {@link NotFoundException}.
     */
    @Test
    @DisplayName("getClientByEmail: should throw NotFoundException if the client is not found")
    void whenGetClientByEmail_whenNotFound_thenThrowNotFoundException() {
        // Arrange
        String nonExistentEmail = "notfound@example.com";
        when(clientRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(nonExistentEmail));
        verify(clientMapper, never()).toDTO(any());
    }

    /**
     * A parameterized test that validates both {@code blockClient} and {@code unblockClient} methods.
     * It checks that the {@code enabled} status of the client entity is correctly modified and
     * that the updated entity is saved.
     *
     * @param action         The name of the action to perform ("block" or "unblock").
     * @param expectedStatus The expected boolean status of the client's {@code enabled} field after the action.
     */
    @ParameterizedTest(name = "Enabled status should be {1} after calling method {0}")
    @DisplayName("blockClient and unblockClient: should change the enabled status")
    @CsvSource({
            "block, false",
            "unblock, true"
    })
    void whenBlockOrUnblockClient_thenChangeEnabledStatus(String action, boolean expectedStatus) {
        // Arrange
        long clientId = 1L;
        Client client = Client.builder().id(clientId).enabled(!expectedStatus).build();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act
        if ("block".equals(action)) {
            clientService.blockClient(clientId);
        } else {
            clientService.unblockClient(clientId);
        }

        // Assert
        assertEquals(expectedStatus, client.isEnabled());
        verify(clientRepository, times(1)).save(client);
    }

    /**
     * Tests the successful top-up of a client's balance.
     * It verifies that the service correctly adds the specified amount to the
     * client's existing balance and saves the result.
     */
    @Test
    @DisplayName("topUpBalance: should correctly increase the client's balance")
    void whenTopUpBalance_withValidAmount_thenIncreaseBalance() {
        // Arrange
        String clientEmail = "test@example.com";
        Client client = Client.builder()
                .email(clientEmail)
                .balance(new BigDecimal("100.00"))
                .build();

        BigDecimal topUpAmount = new BigDecimal("50.50");
        BigDecimal expectedBalance = new BigDecimal("150.50");

        when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));

        // Act
        clientService.topUpBalance(clientEmail, topUpAmount);

        // Assert
        assertEquals(0, expectedBalance.compareTo(client.getBalance()), "The balance was not updated correctly.");
        verify(clientRepository, times(1)).save(client);
    }

    /**
     * A parameterized test to ensure that the balance top-up logic rejects invalid amounts.
     * It verifies that an {@link IllegalArgumentException} is thrown for zero or negative
     * amounts and that no database operations are attempted.
     *
     * @param amountStr The invalid amount to test, as a string.
     */
    @ParameterizedTest
    @DisplayName("topUpBalance: should throw IllegalArgumentException for invalid amount")
    @ValueSource(strings = {"0.00", "-10.00"})
    void whenTopUpBalance_withInvalidAmount_thenThrowIllegalArgumentException(String amountStr) {
        // Arrange
        String clientEmail = "test@example.com";
        BigDecimal invalidAmount = new BigDecimal(amountStr);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clientService.topUpBalance(clientEmail, invalidAmount));

        verify(clientRepository, never()).findByEmail(anyString());
        verify(clientRepository, never()).save(any());
    }
}