package com.example.book.service.mapper;

import com.example.book.service.dto.BookItemDTO;
import com.example.book.service.model.Book;
import com.example.book.service.model.BookItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-27T21:00:04+0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class BookItemMapperImpl implements BookItemMapper {

    @Override
    public BookItemDTO toDTO(BookItem item) {
        if ( item == null ) {
            return null;
        }

        BookItemDTO.BookItemDTOBuilder bookItemDTO = BookItemDTO.builder();

        bookItemDTO.id( itemBookId( item ) );
        bookItemDTO.name( itemBookName( item ) );
        bookItemDTO.author( itemBookAuthor( item ) );
        bookItemDTO.quantity( item.getQuantity() );

        return bookItemDTO.build();
    }

    @Override
    public BookItem toEntity(BookItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BookItem.BookItemBuilder bookItem = BookItem.builder();

        bookItem.quantity( dto.getQuantity() );

        return bookItem.build();
    }

    private Long itemBookId(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getId();
    }

    private String itemBookName(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getName();
    }

    private String itemBookAuthor(BookItem bookItem) {
        Book book = bookItem.getBook();
        if ( book == null ) {
            return null;
        }
        return book.getAuthor();
    }
}
