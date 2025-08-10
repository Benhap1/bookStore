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

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientCreateRequestDTO dto) {
        boolean exists = clientRepository.findByEmail(dto.getEmail()).isPresent();
        if (exists) {
            throw new AlreadyExistException("Client already exists with email: " + dto.getEmail());
        }

        Client client = clientMapper.toEntity(dto);
        client.setPassword(passwordEncoder.encode(dto.getPassword()));
        client.setEnabled(true);
        client.setRole(Role.CLIENT);

        Client saved = clientRepository.save(client);
        return clientMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void blockClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + id));
        client.setEnabled(false);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void unblockClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + id));
        client.setEnabled(true);
        clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(clientMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> searchClientsByEmail(String email) {
        return clientRepository.findByEmailContainingIgnoreCase(email).stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }
}
