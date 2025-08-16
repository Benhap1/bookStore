package com.example.book.service.service.impl;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.ClientMapper;
import com.example.book.service.model.Client;
import com.example.book.service.model.enums.Role;
import com.example.book.service.repo.ClientRepository;
import com.example.book.service.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link ClientService} interface.
 * <p>
 * This class handles all business logic related to client account management.
 * It coordinates interactions with the {@link ClientRepository} and uses the
 * {@link PasswordEncoder} for security. Access to methods is controlled via
 * {@code @PreAuthorize} annotations to distinguish between public, client-only,
 * and admin-only actions.
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * This action is restricted to administrators.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation ensures the email is unique, encodes the password,
     * and sets default values (enabled status, client role) for the new user.
     */
    @Override
    @Transactional
    public ClientDTO addClient(ClientCreateRequestDTO dto) {
        if (clientRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExistException("Client already exists with email: " + dto.getEmail());
        }

        Client client = clientMapper.toEntity(dto);
        client.setPassword(passwordEncoder.encode(dto.getPassword()));
        client.setEnabled(true);
        client.setRole(Role.CLIENT);

        Client saved = clientRepository.save(client);
        return clientMapper.toDTO(saved);
    }

    /**
     * {@inheritDoc}
     * This sets the client's 'enabled' status to false.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void blockClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with id: ".concat(String.valueOf(id))));
        client.setEnabled(false);
        clientRepository.save(client);
    }

    /**
     * {@inheritDoc}
     * This sets the client's 'enabled' status to true.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void unblockClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with id: ".concat(String.valueOf(id))));
        client.setEnabled(true);
        clientRepository.save(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(clientMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method is secured to ensure that an authenticated client can only top up
     * their own balance. It also validates that the top-up amount is positive.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('CLIENT') and #clientEmail == authentication.name")
    public void topUpBalance(String clientEmail, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive.");
        }

        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + clientEmail));

        client.setBalance(client.getBalance().add(amount));
        clientRepository.save(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> searchClientsByEmail(String email) {
        return clientRepository.findByEmailContainingIgnoreCase(email).stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }
}