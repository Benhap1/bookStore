package com.example.book.service.mapper;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.model.BookItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookItemMapper {

    @Mapping(source = "book.id", target = "id")
    @Mapping(source = "book.name", target = "name")
    @Mapping(source = "book.author", target = "author")
    BookItemDTO toDTO(BookItem item);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "order", ignore = true)
    BookItem toEntity(BookItemDTO dto);
}
