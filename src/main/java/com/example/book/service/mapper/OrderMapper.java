package com.example.book.service.mapper;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.dto.OrderDTO;
import com.example.book.service.model.BookItem;
import com.example.book.service.model.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "client.email", target = "clientEmail")
    @Mapping(source = "client.balance", target = "clientBalance")
    OrderDTO toDTO(Order order);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "bookItems", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "price", ignore = true)
    Order toEntity(OrderDTO dto);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "book.name", target = "name")
    @Mapping(source = "book.author", target = "author")
    @Mapping(source = "book.price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    BookItemDTO bookItemToBookItemDTO(BookItem bookItem);
}


