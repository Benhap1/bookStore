package com.example.book.service.service;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;

import java.util.List;

public interface ClientService {
    List<ClientDTO> getAllClients();

    ClientDTO addClient(ClientCreateRequestDTO client);

    ClientDTO getClientById(Long id);

    ClientDTO updateClientById(Long id, ClientDTO clientDTO);

    void deleteClientById(Long id);

    void blockClient(Long id);

    void unblockClient(Long id);
}
