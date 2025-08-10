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

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;


    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + id));
        return bookMapper.toDTO(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BookDTO updateBookById(Long id, BookDTO bookDTO) {
        Book bookToUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id: " + id));
        if (!bookToUpdate.getName().equals(bookDTO.getName())) {
            bookRepository.findByName(bookDTO.getName()).ifPresent(b -> {
                throw new AlreadyExistException("A book with the name '" + bookDTO.getName() + "' already exists.");
            });
        }

        bookMapper.updateBookFromDTO(bookDTO, bookToUpdate);
        Book saved = bookRepository.save(bookToUpdate);
        return bookMapper.toDTO(saved);
    }



    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with name: " + id));
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BookDTO addBook(BookDTO bookDTO) {
        bookRepository.findByName(bookDTO.getName()).ifPresent(b -> {
            throw new AlreadyExistException("A book with the name '" + bookDTO.getName() + "' already exists.");
        });

        Book book = bookMapper.toEntity(bookDTO);
        Book saved = bookRepository.save(book);
        return bookMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword).stream()
                .map(bookMapper::toDTO)
                .collect(Collectors.toList());
    }
}
