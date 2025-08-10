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

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/list")
    public String getAllBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books = findBooksByKeyword(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/list";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books = findBooksByKeyword(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/admin-list";
    }

    private List<BookDTO> findBooksByKeyword(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return bookService.searchBooks(keyword);
        }
        return bookService.getAllBooks();
    }


    @GetMapping("/view/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "books/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new BookDTO());
        model.addAttribute("pageTitle", "Add New Book");
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "books/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        model.addAttribute("pageTitle", "Edit Book");
        model.addAttribute("ageGroups", AgeGroup.values());
        model.addAttribute("languages", Language.values());
        return "books/form";
    }


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


    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBookById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting book. It might be in use in an order.");
        }
        return "redirect:/books/manage";
    }
}