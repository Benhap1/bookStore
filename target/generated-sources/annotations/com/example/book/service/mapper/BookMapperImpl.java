package com.example.book.service.mapper;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.model.Book;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-10T21:56:57+0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public BookDTO toDTO(Book book) {
        if ( book == null ) {
            return null;
        }

        BookDTO.BookDTOBuilder bookDTO = BookDTO.builder();

        bookDTO.id( book.getId() );
        bookDTO.name( book.getName() );
        bookDTO.genre( book.getGenre() );
        bookDTO.targetAgeGroup( book.getTargetAgeGroup() );
        bookDTO.price( book.getPrice() );
        bookDTO.publicationDate( book.getPublicationDate() );
        bookDTO.author( book.getAuthor() );
        bookDTO.pages( book.getPages() );
        bookDTO.characteristics( book.getCharacteristics() );
        bookDTO.description( book.getDescription() );
        bookDTO.language( book.getLanguage() );

        return bookDTO.build();
    }

    @Override
    public Book toEntity(BookDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Book.BookBuilder book = Book.builder();

        book.name( dto.getName() );
        book.genre( dto.getGenre() );
        book.targetAgeGroup( dto.getTargetAgeGroup() );
        book.price( dto.getPrice() );
        book.publicationDate( dto.getPublicationDate() );
        book.author( dto.getAuthor() );
        book.pages( dto.getPages() );
        book.characteristics( dto.getCharacteristics() );
        book.description( dto.getDescription() );
        book.language( dto.getLanguage() );

        return book.build();
    }

    @Override
    public void updateBookFromDTO(BookDTO dto, Book entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
        if ( dto.getGenre() != null ) {
            entity.setGenre( dto.getGenre() );
        }
        if ( dto.getTargetAgeGroup() != null ) {
            entity.setTargetAgeGroup( dto.getTargetAgeGroup() );
        }
        if ( dto.getPrice() != null ) {
            entity.setPrice( dto.getPrice() );
        }
        if ( dto.getPublicationDate() != null ) {
            entity.setPublicationDate( dto.getPublicationDate() );
        }
        if ( dto.getAuthor() != null ) {
            entity.setAuthor( dto.getAuthor() );
        }
        if ( dto.getPages() != null ) {
            entity.setPages( dto.getPages() );
        }
        if ( dto.getCharacteristics() != null ) {
            entity.setCharacteristics( dto.getCharacteristics() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getLanguage() != null ) {
            entity.setLanguage( dto.getLanguage() );
        }
    }
}
