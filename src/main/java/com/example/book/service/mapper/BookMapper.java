package com.example.book.service.mapper;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.model.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDTO toDTO(Book book);
    @Mapping(target = "id", ignore = true)
    Book toEntity(BookDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateBookFromDTO(BookDTO dto, @MappingTarget Book entity);
}

