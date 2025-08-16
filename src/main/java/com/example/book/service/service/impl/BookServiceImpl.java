package com.example.book.service.service.impl;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.exception.NotFoundException;
import com.example.book.service.mapper.BookMapper;
import com.example.book.service.model.Book;
import com.example.book.service.repo.BookRepository;
import com.example.book.service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The concrete implementation of the {@link BookService} interface.
 * <p>
 * This class handles the business logic for book management and interacts
 * with the {@link BookRepository} to perform database operations. It uses MapStruct
 * for mapping between entities and DTOs.
 * <p>
 * Methods that modify data (add, update, delete) are secured at the method level
 * with {@code @PreAuthorize("hasRole('ADMIN')")}, ensuring that only administrators
 * can manage the book catalog. All data-modifying methods are also marked as
 * {@code @Transactional} to ensure data consistency.
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + id));
        return bookMapper.toDTO(book);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation includes a crucial validation step: it checks if the new
     * book name is already in use by another book before attempting to save.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BookDTO updateBookById(Long id, BookDTO bookDTO) {
        // Find the book to be updated.
        Book bookToUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: ".concat(String.valueOf(id))));

        // If the name is being changed, we must check if the new name is already taken.
        if (!bookToUpdate.getName().equals(bookDTO.getName())) {
            bookRepository.findByName(bookDTO.getName()).ifPresent(b -> {
                throw new AlreadyExistException("A book with the name '" + bookDTO.getName() + "' already exists.");
            });
        }

        // Use the mapper to update the entity with new data from the DTO.
        bookMapper.updateBookFromDTO(bookDTO, bookToUpdate);

        // Save the updated entity and return its DTO representation.
        Book saved = bookRepository.save(bookToUpdate);
        return bookMapper.toDTO(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBookById(Long id) {
        // First, check if the book exists to provide a clear error message.
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: ".concat(String.valueOf(id))));
        bookRepository.delete(book);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation ensures that a new book cannot be created with a name
     * that already exists in the database.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BookDTO addBook(BookDTO bookDTO) {
        // Business rule: Book names must be unique.
        bookRepository.findByName(bookDTO.getName()).ifPresent(b -> {
            throw new AlreadyExistException("A book with the name '" + bookDTO.getName() + "' already exists.");
        });

        // Convert DTO to entity, save it, and return the new DTO.
        Book book = bookMapper.toEntity(bookDTO);
        Book saved = bookRepository.save(book);
        return bookMapper.toDTO(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword).stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }
}