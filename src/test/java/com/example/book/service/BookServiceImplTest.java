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
 * Unit tests for the {@link BookServiceImpl} class.
 * <p>
 * This test class focuses on verifying the business logic of the book service in isolation.
 * It uses Mockito to create mock objects for external dependencies (like {@link BookRepository}
 * and {@link BookMapper}), ensuring that the tests are fast, predictable, and focused solely
 * on the service layer's behavior.
 *
 * @see BookServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    /**
     * A mock of the {@link BookRepository}. It simulates database interactions,
     * allowing tests to control the data returned from repository calls.
     */
    @Mock
    private BookRepository bookRepository;

    /**
     * A mock of the {@link BookMapper}. It simulates the mapping between
     * {@code Book} entities and {@code BookDTO} data transfer objects.
     */
    @Mock
    private BookMapper bookMapper;

    /**
     * The instance of {@link BookServiceImpl} to be tested. Mockito will automatically
     * inject the mocked dependencies ({@code bookRepository}, {@code bookMapper}) into this instance.
     */
    @InjectMocks
    private BookServiceImpl bookService;

    /**
     * Tests the successful retrieval of a book by its ID.
     * It verifies that when a book with the given ID exists in the repository,
     * the service correctly returns the corresponding {@link BookDTO}.
     */
    @Test
    @DisplayName("getBookById: should return BookDTO when the book exists")
    void whenGetBookById_whenBookExists_thenReturnBookDTO() {
        // Arrange: Set up mock data and behavior
        long bookId = 1L;
        Book book = Book.builder().id(bookId).name("The Hobbit").build();
        BookDTO bookDTO = BookDTO.builder().id(bookId).name("The Hobbit").build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        // Act: Call the method under test
        BookDTO foundBook = bookService.getBookById(bookId);

        // Assert: Verify the results
        assertNotNull(foundBook);
        assertEquals(bookId, foundBook.getId());
        assertEquals("The Hobbit", foundBook.getName());

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toDTO(book);
    }

    /**
     * Tests the scenario where a requested book ID does not exist.
     * It verifies that the service correctly throws a {@link NotFoundException}
     * when the repository returns an empty {@code Optional}.
     */
    @Test
    @DisplayName("getBookById: should throw NotFoundException when the book does not exist")
    void whenGetBookById_whenBookDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        long bookId = 99L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bookService.getBookById(bookId);
        });

        // Verify that the mapper was never called, as the process should fail before mapping.
        verify(bookMapper, never()).toDTO(any());
    }

    /**
     * Tests the successful creation of a new book.
     * It verifies that if a book with the given name does not already exist,
     * the service correctly maps, saves, and returns the new book as a DTO.
     */
    @Test
    @DisplayName("addBook: should successfully save a new book")
    void whenAddBook_withUniqueName_thenSaveAndReturnDTO() {
        // Arrange
        BookDTO bookToSaveDTO = BookDTO.builder().name("New Unique Book").build();
        Book bookToSave = Book.builder().name("New Unique Book").build();
        Book savedBook = Book.builder().id(1L).name("New Unique Book").build();
        BookDTO savedBookDTO = BookDTO.builder().id(1L).name("New Unique Book").build();

        when(bookRepository.findByName("New Unique Book")).thenReturn(Optional.empty());
        when(bookMapper.toEntity(bookToSaveDTO)).thenReturn(bookToSave);
        when(bookRepository.save(bookToSave)).thenReturn(savedBook);
        when(bookMapper.toDTO(savedBook)).thenReturn(savedBookDTO);

        // Act
        BookDTO result = bookService.addBook(bookToSaveDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository, times(1)).save(bookToSave);
    }

    /**
     * Tests the business rule preventing the creation of a book with a duplicate name.
     * It verifies that the service throws an {@link AlreadyExistException}
     * and does not attempt to save the new book.
     */
    @Test
    @DisplayName("addBook: should throw AlreadyExistException if the name is already taken")
    void whenAddBook_withExistingName_thenThrowAlreadyExistException() {
        // Arrange
        BookDTO bookToSaveDTO = BookDTO.builder().name("Existing Book").build();
        Book existingBook = Book.builder().id(1L).name("Existing Book").build();

        when(bookRepository.findByName("Existing Book")).thenReturn(Optional.of(existingBook));

        // Act & Assert
        assertThrows(AlreadyExistException.class, () -> {
            bookService.addBook(bookToSaveDTO);
        });

        // Verify that the save method was never called.
        verify(bookRepository, never()).save(any());
    }

    /**
     * Tests the successful update of an existing book.
     * It verifies that the service finds the book, delegates the update to the mapper,
     * saves the changes, and returns the updated DTO.
     */
    @Test
    @DisplayName("updateBookById: should successfully update a book")
    void whenUpdateBook_whenExists_thenUpdateAndReturnDTO() {
        // Arrange
        long bookId = 1L;
        BookDTO bookUpdatesDTO = BookDTO.builder().id(bookId).name("Updated Name").price(BigDecimal.TEN).build();
        Book existingBook = new Book(); // Using a real object to verify state changes
        existingBook.setId(bookId);
        existingBook.setName("Old Name");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook); // Mock save to return the same object
        when(bookMapper.toDTO(any(Book.class))).thenReturn(bookUpdatesDTO);

        // Act
        BookDTO updatedBook = bookService.updateBookById(bookId, bookUpdatesDTO);

        // Assert
        assertNotNull(updatedBook);
        assertEquals("Updated Name", updatedBook.getName());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(existingBook);
        // Crucially, verify that the mapper was called to apply the updates.
        verify(bookMapper, times(1)).updateBookFromDTO(bookUpdatesDTO, existingBook);
    }

    /**
     * Tests the successful deletion of an existing book.
     * It verifies that if a book is found by its ID, the repository's {@code delete}
     * method is correctly invoked with the found entity.
     */
    @Test
    @DisplayName("deleteBookById: should call delete method if the book exists")
    void whenDeleteBook_whenExists_thenCallDelete() {
        // Arrange
        long bookId = 1L;
        Book book = Book.builder().id(bookId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.deleteBookById(bookId);

        // Assert
        verify(bookRepository, times(1)).delete(book);
    }

    /**
     * A parameterized test for the book search functionality.
     * It verifies that when the {@code searchBooks} method is called with any non-blank keyword,
     * it correctly invokes the specific {@code searchBooks} method on the repository and
     * does not fall back to fetching all books.
     *
     * @param keyword The search term to test.
     */
    @ParameterizedTest
    @DisplayName("searchBooks: should call the correct repository method")
    @ValueSource(strings = {"Hobbit", "1984", "Dune"})
    void whenSearchBooks_withKeyword_thenCallRepositorySearch(String keyword) {
        // Arrange
        when(bookRepository.searchBooks(keyword)).thenReturn(Collections.emptyList());

        // Act
        bookService.searchBooks(keyword);

        // Assert
        verify(bookRepository, times(1)).searchBooks(keyword);
        verify(bookRepository, never()).findAll(); // Ensure the "get all" method is not called
    }
}