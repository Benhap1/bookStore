package com.example.book.service.service;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {
    List<ClientDTO> getAllClients();

    ClientDTO addClient(ClientCreateRequestDTO client);

    void blockClient(Long id);

    void unblockClient(Long id);

    ClientDTO getClientByEmail(String email);

    void topUpBalance(String clientEmail, BigDecimal amount);

    List<ClientDTO> searchClientsByEmail(String email);
}
