package com.example.book.service.service;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Defines the contract for business operations related to clients (users).
 * <p>
 * This service provides a high-level API for managing client accounts, including
 * creation, administrative actions (blocking/unblocking), and client-specific
 * actions like managing their balance.
 */
public interface ClientService {

    /**
     * Retrieves a list of all clients. This is an administrative action.
     *
     * @return A list of all {@link ClientDTO} objects.
     */
    List<ClientDTO> getAllClients();

    /**
     * Creates a new client account. This action is typically available to the public.
     *
     * @param client The DTO containing the new client's registration data.
     * @return A {@link ClientDTO} representing the newly created client.
     */
    ClientDTO addClient(ClientCreateRequestDTO client);

    /**
     * Blocks a client's account, preventing them from logging in. Administrative action.
     *
     * @param id The ID of the client to block.
     */
    void blockClient(Long id);

    /**
     * Unblocks a client's account, allowing them to log in again. Administrative action.
     *
     * @param id The ID of the client to unblock.
     */
    void unblockClient(Long id);

    /**
     * Retrieves a client's details by their unique email address.
     *
     * @param email The email of the client to find.
     * @return A {@link ClientDTO} for the found client.
     */
    ClientDTO getClientByEmail(String email);

    /**
     * Adds a specified amount to a client's balance.
     *
     * @param clientEmail The email of the client whose balance is to be topped up.
     * @param amount      The positive amount to add to the balance.
     * @throws IllegalArgumentException if the amount is not a positive value.
     */
    void topUpBalance(String clientEmail, BigDecimal amount);

    /**
     * Searches for clients whose email contains the given keyword. Case-insensitive.
     *
     * @param email The keyword to search for within client emails.
     * @return A list of matching {@link ClientDTO} objects.
     */
    List<ClientDTO> searchClientsByEmail(String email);
}
