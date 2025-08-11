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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    @DisplayName("addClient: should successfully create a client if the email is unique")
    void whenAddClient_withUniqueEmail_thenCreateClient() {

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

        ClientDTO result = clientService.addClient(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("new@example.com", result.getEmail());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(clientMapper).toEntity(createDTO);
        verify(clientRepository, times(1)).save(clientToSave);

        assertEquals(Role.CLIENT, clientToSave.getRole());
        assertTrue(clientToSave.isEnabled());
    }

    @Test
    @DisplayName("addClient: should throw AlreadyExistException if the email already exists")
    void whenAddClient_withExistingEmail_thenThrowAlreadyExistException() {

        ClientCreateRequestDTO createDTO = ClientCreateRequestDTO.builder().email("existing@example.com").build();
        Client existingClient = new Client();

        when(clientRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingClient));

        assertThrows(AlreadyExistException.class, () -> {
            clientService.addClient(createDTO);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("getClientByEmail: should throw NotFoundException if the client is not found")
    void whenGetClientByEmail_whenNotFound_thenThrowNotFoundException() {
        String nonExistentEmail = "notfound@example.com";

        when(clientRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            clientService.getClientByEmail(nonExistentEmail);
        });

        verify(clientMapper, never()).toDTO(any());
    }

    @ParameterizedTest(name = "Enabled status should be {1} after calling method {0}")
    @DisplayName("blockClient and unblockClient: should change the enabled status")
    @CsvSource({
            "block, false",
            "unblock, true"
    })
    void whenBlockOrUnblockClient_thenChangeEnabledStatus(String action, boolean expectedStatus) {
        long clientId = 1L;
        Client client = Client.builder().id(clientId).enabled(!expectedStatus).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        if ("block".equals(action)) {
            clientService.blockClient(clientId);
        } else {
            clientService.unblockClient(clientId);
        }

        assertEquals(expectedStatus, client.isEnabled());
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("topUpBalance: should correctly increase the client's balance")
    void whenTopUpBalance_withValidAmount_thenIncreaseBalance() {
        // --- Given ---
        String clientEmail = "test@example.com";
        Client client = Client.builder()
                .email(clientEmail)
                .balance(new java.math.BigDecimal("100.00"))
                .build();

        java.math.BigDecimal topUpAmount = new java.math.BigDecimal("50.50");
        java.math.BigDecimal expectedBalance = new java.math.BigDecimal("150.50");

        when(clientRepository.findByEmail(clientEmail)).thenReturn(Optional.of(client));

        clientService.topUpBalance(clientEmail, topUpAmount);

        assertEquals(0, expectedBalance.compareTo(client.getBalance()));
        verify(clientRepository, times(1)).save(client);
    }

    @ParameterizedTest
    @DisplayName("topUpBalance: should throw IllegalArgumentException for invalid amount")
    @ValueSource(strings = {"0.00", "-10.00"})
    void whenTopUpBalance_withInvalidAmount_thenThrowIllegalArgumentException(String amountStr) {

        String clientEmail = "test@example.com";
        java.math.BigDecimal invalidAmount = new java.math.BigDecimal(amountStr);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.topUpBalance(clientEmail, invalidAmount);
        });

        verify(clientRepository, never()).findByEmail(anyString());
        verify(clientRepository, never()).save(any());
    }


    @Test
    @DisplayName("getClientByEmail: should return ClientDTO when the client exists")
    void whenGetClientByEmail_whenExists_thenReturnClientDTO() {
        String existingEmail = "found@example.com";
        Client client = Client.builder().id(1L).email(existingEmail).build();
        ClientDTO expectedDTO = ClientDTO.builder().id(1L).email(existingEmail).build();

        when(clientRepository.findByEmail(existingEmail)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(expectedDTO);

        ClientDTO result = clientService.getClientByEmail(existingEmail);

        assertNotNull(result);
        assertEquals(expectedDTO.getId(), result.getId());
        assertEquals(expectedDTO.getEmail(), result.getEmail());

        verify(clientRepository, times(1)).findByEmail(existingEmail);
        verify(clientMapper, times(1)).toDTO(client);
    }
}
