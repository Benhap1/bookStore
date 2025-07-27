package com.example.book.service.service;

import com.example.book.service.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("clientSecurityService")
@RequiredArgsConstructor
public class ClientSecurityService {

    private final ClientRepository clientRepository;

    public boolean isAccountOwner(Authentication authentication, Long id) {
        String currentUserEmail = authentication.getName();
        return clientRepository.findById(id)
                .map(client -> client.getEmail().equals(currentUserEmail))
                .orElse(false);
    }
}
