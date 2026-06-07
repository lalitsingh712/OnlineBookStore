package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.payload.response.MessageResponse;
import com.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookService bookService;

    // Public API to get catalog
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        if (book.isPresent()) {
            return ResponseEntity.ok(book.get());
        } else {
            return ResponseEntity.status(404).body(new MessageResponse("Book not found"));
        }
    }

    // Protected APIs for Admin
    @PostMapping("/admin/books")
    @PreAuthorize("hasRole('ADMIN')")
    public Book addBook(@RequestBody Book book) {
        return bookService.saveBook(book);
    }

    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        Optional<Book> bookData = bookService.getBookById(id);

        if (bookData.isPresent()) {
            Book _book = bookData.get();
            _book.setTitle(bookDetails.getTitle());
            _book.setAuthor(bookDetails.getAuthor());
            _book.setCategory(bookDetails.getCategory());
            _book.setPrice(bookDetails.getPrice());
            _book.setImageUrl(bookDetails.getImageUrl());
            return ResponseEntity.ok(bookService.saveBook(_book));
        } else {
            return ResponseEntity.status(404).body(new MessageResponse("Book not found"));
        }
    }

    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(new MessageResponse("Book deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Could not delete book"));
        }
    }
}
