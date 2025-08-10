package com.example.book.service.controller;

import com.example.book.service.dto.BookDTO;
import com.example.book.service.exception.AlreadyExistException;
import com.example.book.service.model.enums.AgeGroup;
import com.example.book.service.model.enums.Language;
import com.example.book.service.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.book.service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

//@RestController
//@RequestMapping("/api/books")
//@RequiredArgsConstructor
//public class BookController {
//
//    private final BookService bookService;
//
//    @GetMapping
//    public List<BookDTO> getAllBooks() {
//        return bookService.getAllBooks();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
//        return ResponseEntity.ok(bookService.getBookById(id));
//    }
//
//    @PostMapping
//public ResponseEntity<BookDTO> addBook(@RequestBody @Valid BookDTO bookDTO) {
//    BookDTO createdBook = bookService.addBook(bookDTO);
//    URI location = ServletUriComponentsBuilder
//            .fromCurrentRequest()
//            .path("/{id}")
//            .buildAndExpand(createdBook.getId())
//            .toUri();
//    return ResponseEntity.created(location).body(createdBook);
//}
//
//    @PutMapping("/{id}")
//    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id,
//                                              @RequestBody @Valid BookDTO bookDTO) {
//        return ResponseEntity.ok(bookService.updateBookById(id, bookDTO));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
//        bookService.deleteBookById(id);
//        return ResponseEntity.noContent().build();
//    }
//}

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

//    @GetMapping("/list")
//    public String getAllBooks(Model model) {
//        model.addAttribute("books", bookService.getAllBooks());
//        return "books/list";
//    }

    @GetMapping("/list")
    public String getAllBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books;
        if (keyword != null && !keyword.isBlank()) {
            books = bookService.searchBooks(keyword);
        } else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/list";
    }

    @GetMapping("/view/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "books/view";
    }



    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<BookDTO> books;
        if (keyword != null && !keyword.isBlank()) {
            books = bookService.searchBooks(keyword);
        } else {
            books = bookService.getAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/admin-list";
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