package com.example.book.service.mapper;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;
import com.example.book.service.model.Client;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-27T21:00:04+0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public ClientDTO toDTO(Client client) {
        if ( client == null ) {
            return null;
        }

        ClientDTO.ClientDTOBuilder clientDTO = ClientDTO.builder();

        clientDTO.id( client.getId() );
        clientDTO.email( client.getEmail() );
        clientDTO.firstName( client.getFirstName() );
        clientDTO.lastName( client.getLastName() );
        clientDTO.balance( client.getBalance() );
        clientDTO.enabled( client.getEnabled() );

        return clientDTO.build();
    }

    @Override
    public Client toEntity(ClientCreateRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Client.ClientBuilder<?, ?> client = Client.builder();

        client.firstName( dto.getFirstName() );
        client.lastName( dto.getLastName() );
        client.email( dto.getEmail() );
        client.password( dto.getPassword() );
        client.balance( dto.getBalance() );

        client.role( com.example.book.service.model.enums.Role.CLIENT );
        client.enabled( true );

        return client.build();
    }

    @Override
    public void updateClientFromDTO(ClientDTO dto, Client entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getEmail() != null ) {
            entity.setEmail( dto.getEmail() );
        }
        if ( dto.getEnabled() != null ) {
            entity.setEnabled( dto.getEnabled() );
        }
        if ( dto.getBalance() != null ) {
            entity.setBalance( dto.getBalance() );
        }
    }
}
