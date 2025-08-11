package com.example.book.service;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.BookMapper;
import com.example.book.service.model.Book;
import com.example.book.service.repo.BookRepository;
import com.example.book.service.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-tests for BookServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("getBookById: should return BookDTO when the book exists")
    void whenGetBookById_whenBookExists_thenReturnBookDTO() {

        long bookId = 1L;
        Book book = Book.builder().id(bookId).name("The Hobbit").build();
        BookDTO bookDTO = BookDTO.builder().id(bookId).name("The Hobbit").build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        BookDTO foundBook = bookService.getBookById(bookId);

        assertNotNull(foundBook);
        assertEquals(bookId, foundBook.getId());
        assertEquals("The Hobbit", foundBook.getName());

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toDTO(book);
    }

    @Test
    @DisplayName("getBookById: should throw NotFoundException when the book does not exist")
    void whenGetBookById_whenBookDoesNotExist_thenThrowNotFoundException() {

        long bookId = 99L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookService.getBookById(bookId);
        });

        verify(bookMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("addBook: should successfully save a new book")
    void whenAddBook_withUniqueName_thenSaveAndReturnDTO() {

        BookDTO bookToSaveDTO = BookDTO.builder().name("New Unique Book").build();
        Book bookToSave = Book.builder().name("New Unique Book").build();
        Book savedBook = Book.builder().id(1L).name("New Unique Book").build();
        BookDTO savedBookDTO = BookDTO.builder().id(1L).name("New Unique Book").build();

        when(bookRepository.findByName("New Unique Book")).thenReturn(Optional.empty());
        when(bookMapper.toEntity(bookToSaveDTO)).thenReturn(bookToSave);
        when(bookRepository.save(bookToSave)).thenReturn(savedBook);
        when(bookMapper.toDTO(savedBook)).thenReturn(savedBookDTO);

        BookDTO result = bookService.addBook(bookToSaveDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository, times(1)).save(bookToSave);
    }

    @Test
    @DisplayName("addBook: should throw AlreadyExistException if the name is already taken")
    void whenAddBook_withExistingName_thenThrowAlreadyExistException() {

        BookDTO bookToSaveDTO = BookDTO.builder().name("Existing Book").build();
        Book existingBook = Book.builder().id(1L).name("Existing Book").build();

        when(bookRepository.findByName("Existing Book")).thenReturn(Optional.of(existingBook));

        assertThrows(AlreadyExistException.class, () -> {
            bookService.addBook(bookToSaveDTO);
        });

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateBookById: should successfully update a book")
    void whenUpdateBook_whenExists_thenUpdateAndReturnDTO() {

        long bookId = 1L;
        BookDTO bookUpdatesDTO = BookDTO.builder().id(bookId).name("Updated Name").price(BigDecimal.TEN).build();
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setName("Old Name");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookUpdatesDTO);

        BookDTO updatedBook = bookService.updateBookById(bookId, bookUpdatesDTO);

        assertNotNull(updatedBook);
        assertEquals("Updated Name", updatedBook.getName());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(existingBook);
        verify(bookMapper, times(1)).updateBookFromDTO(bookUpdatesDTO, existingBook);
    }

    @Test
    @DisplayName("deleteBookById: should call delete method if the book exists")
    void whenDeleteBook_whenExists_thenCallDelete() {
        long bookId = 1L;
        Book book = Book.builder().id(bookId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.deleteBookById(bookId);

        verify(bookRepository, times(1)).delete(book);
    }

    @ParameterizedTest
    @DisplayName("searchBooks: should call the correct repository method")
    @ValueSource(strings = {"Hobbit", "1984", "Dune"})
    void whenSearchBooks_withKeyword_thenCallRepositorySearch(String keyword) {

        when(bookRepository.searchBooks(keyword)).thenReturn(Collections.emptyList());

        bookService.searchBooks(keyword);

        verify(bookRepository, times(1)).searchBooks(keyword);
        verify(bookRepository, never()).findAll();
    }
}
