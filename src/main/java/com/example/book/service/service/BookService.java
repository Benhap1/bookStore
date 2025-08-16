package com.example.book.service.service;

import com.example.book.service.dto.BookDTO;

import java.util.List;

/**
 * Defines the contract for business operations related to books.
 * <p>
 * This service provides a high-level API for managing the book catalog,
 * including CRUD (Create, Read, Update, Delete) operations and search functionality.
 * Implementations of this interface are responsible for handling all business logic,
 * validation, and transaction management associated with {@code Book} entities.
 *
 * @author Your Name // Optional: Add author tag
 * @version 1.0
 */
public interface BookService {

    /**
     * Retrieves a book by its unique identifier.
     *
     * @param id The ID of the book to retrieve. Must not be null.
     * @return A {@link BookDTO} representing the found book.
     */
    BookDTO getBookById(Long id);

    /**
     * Updates an existing book's details based on its ID.
     *
     * @param id      The ID of the book to update. Must not be null.
     * @param bookDTO A DTO containing the new data for the book. Must not be null.
     * @return A {@link BookDTO} representing the state of the book after the update.
     */
    BookDTO updateBookById(Long id, BookDTO bookDTO);

    /**
     * Deletes a book from the system by its unique identifier.
     *
     * @param id The ID of the book to delete. Must not be null.
     * @// Note: This operation may fail if the book is part of an existing order,
     * // depending on foreign key constraints.
     */
    void deleteBookById(Long id);

    /**
     * Retrieves a list of all books currently in the catalog.
     *
     * @return A {@code List<BookDTO>} containing all books. The list may be empty
     *         if no books are in the system.
     */
    List<BookDTO> getAllBooks();

    /**
     * Adds a new book to the catalog.
     *
     * @param book A DTO containing the details of the new book to create. Must not be null.
     * @return A {@link BookDTO} representing the newly created book, including its generated ID.
     */
    BookDTO addBook(BookDTO book);

    /**
     * Searches for books based on a provided keyword.
     * <p>
     * The search is typically performed against multiple fields, such as the book's
     * name, author, and genre. The matching is case-insensitive.
     *
     * @param keyword The search term to look for.
     * @return A {@code List<BookDTO>} of books matching the keyword. The list may be empty
     *         if no matches are found.
     */
    List<BookDTO> searchBooks(String keyword);
}