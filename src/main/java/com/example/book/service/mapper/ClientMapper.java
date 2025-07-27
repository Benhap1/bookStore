package com.example.book.service.mapper;

import com.example.book.service.dto.ClientCreateRequestDTO;
import com.example.book.service.dto.ClientDTO;
import com.example.book.service.model.Client;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientDTO toDTO(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(com.example.book.service.model.enums.Role.CLIENT)")
    @Mapping(target = "enabled", constant = "true")
    Client toEntity(ClientCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateClientFromDTO(ClientDTO dto, @MappingTarget Client entity);
}


