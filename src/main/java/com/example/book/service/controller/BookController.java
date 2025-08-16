package com.example.book.service.controller;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.model.enums.AgeGroup;
import com.example.book.service.model.enums.Language;
import com.example.book.service.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * Controller for handling all web requests related to books.
 * <p>
 * This controller serves a dual purpose:
 * <ul>
 *   <li><b>Public-facing pages:</b> It provides endpoints for any user to browse a list of books
 *   ({@code /list}) and view the details of a specific book ({@code /view/{id}}). These
 *   endpoints are publicly accessible.</li>
 *   <li><b>Administrator-only pages:</b> It provides a full set of CRUD (Create, Read, Update, Delete)
 *   operations for managing the book catalog. These endpoints are secured with
 *   {@code @PreAuthorize("hasRole('ADMIN')")} to ensure only administrators can access them.</li>
 * </ul>
 * The controller follows the best practice of delegating all business logic to the {@link BookService}.
 */
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Displays the public list of books, with an optional search functionality.
     *
     * @param keyword An optional search term to filter books by name, author, or genre.
     * @param model   The {@link Model} to which the list of books and search keyword are added.
     * @return The view name for the public book listing page ("books/list").
     */
    @GetMapping("/list")
    public String getAllBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books = findBooksByKeyword(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/list";
    }

    /**
     * Displays the administrator's book management page, with search functionality.
     * This endpoint is restricted to users with the 'ADMIN' role.
     *
     * @param keyword An optional search term to filter books.
     * @param model   The {@link Model} to which the list of books and search keyword are added.
     * @return The view name for the admin book management page ("books/admin-list").
     */
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books = findBooksByKeyword(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/admin-list";
    }

    /**
     * A private helper method to fetch books, avoiding code duplication between
     * the public list and the admin management page.
     *
     * @param keyword The search term. If null or blank, all books are returned.
     * @return A list of {@link BookDTO} objects.
     */
    private List<BookDTO> findBooksByKeyword(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return bookService.searchBooks(keyword);
        }
        return bookService.getAllBooks();
    }

    /**
     * Displays the detailed view of a single book.
     *
     * @param id    The ID of the book to display, extracted from the URL path.
     * @param model The {@link Model} to which the found book object is added.
     * @return The view name for the book detail page ("books/view").
     */
    @GetMapping("/view/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "books/view";
    }

    /**
     * Displays the form for creating a new book.
     * This endpoint is restricted to users with the 'ADMIN' role.
     *
     * @param model The {@link Model} prepared with an empty BookDTO and necessary ENUM values for dropdowns.
     * @return The view name for the book form ("books/form").
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new BookDTO());
        model.addAttribute("pageTitle", "Add New Book");
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "books/form";
    }

    /**
     * Displays the form for editing an existing book.
     * This endpoint is restricted to users with the 'ADMIN' role.
     *
     * @param id    The ID of the book to edit.
     * @param model The {@link Model} populated with the existing book's data.
     * @return The view name for the book form ("books/form").
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        model.addAttribute("pageTitle", "Edit Book");
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "books/form";
    }

    /**
     * Processes the submission of the book creation or update form.
     * This endpoint is restricted to users with the 'ADMIN' role.
     * It checks for an ID on the book DTO to determine whether to create a new book or update an existing one.
     *
     * @param book             The book data submitted from the form.
     * @param bindingResult    Holds the result of the validation.
     * @param redirectAttributes Used to pass success or error messages after a redirect.
     * @param model            The model, used to repopulate the form in case of an error.
     * @return A redirect string to the management page on success, or the form view name on failure.
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveBook(@Valid @ModelAttribute("book") BookDTO book,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", book.getId() == null ? "Add New Book" : "Edit Book");
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            return "books/form";
        }

        try {
            if (book.getId() == null) {
                bookService.addBook(book);
                redirectAttributes.addFlashAttribute("successMessage", "Book added successfully!");
            } else {
                bookService.updateBookById(book.getId(), book);
                redirectAttributes.addFlashAttribute("successMessage", "Book updated successfully!");
            }
        } catch (AlreadyExistException e) {
            bindingResult.rejectValue("name", "error.book", e.getMessage());
            model.addAttribute("pageTitle", book.getId() == null ? "Add New Book" : "Edit Book");
            model.addAttribute("ageGroups", AgeGroup.values());
            model.addAttribute("languages", Language.values());
            return "books/form";
        }

        return "redirect:/books/manage";
    }

    /**
     * Handles the deletion of a book by its ID.
     * This endpoint is restricted to users with the 'ADMIN' role.
     *
     * @param id                 The ID of the book to delete.
     * @param redirectAttributes Used to pass a success or error message after the redirect.
     * @return A redirect string to the book management page.
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBookById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully.");
        } catch (Exception e) {
            // Catches potential DataIntegrityViolationException if the book is part of an order.
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting book. It might be in use in an order.");
        }
        return "redirect:/books/manage";
    }
}