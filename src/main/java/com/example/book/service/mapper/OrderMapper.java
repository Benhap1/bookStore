package com.example.book.service.mapper;

import com.example.book.service.dto.OrderDTO;
import com.example.book.service.model.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = BookItemMapper.class)
public interface OrderMapper {

    @Mapping(source = "client.email", target = "clientEmail")
    OrderDTO toDTO(Order order);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "bookItems", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "price", ignore = true)
    Order toEntity(OrderDTO dto);
}


