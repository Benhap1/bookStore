package com.example.book.service.service;

import com.example.book.service.dto.BookDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface BookService {

    BookDTO getBookById(Long id);

    BookDTO updateBookById(Long id, BookDTO bookDTO);

    void deleteBookById(Long id);


    List<BookDTO> getAllBooks();


    BookDTO addBook(BookDTO book);
}
