package com.library.main;

import com.library.config.AppConfig;
import com.library.enums.BookStatus;
import com.library.enums.Genre;
import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;

public class BookController {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        BookService bookService = context.getBean(BookService.class);

        Long authorId = 1L;
        Long memberId = 1L;

        Book book = new Book();
        book.setTitle("1984");
        book.setGenre(Genre.FICTION);
        book.setStatus(BookStatus.BORROWED);
        book.setPublishedYear(1949);

        // 1. save(book entity)
        bookService.save(authorId, memberId, book);
        System.out.println("record inserted...");

        // 2. findById(id)
        Long searchId = 1L;
        Optional<Book> optionalBook = bookService.findById(searchId);
        if (optionalBook.isPresent()) {
            Book b = optionalBook.get();
            System.out.println("Found Book: " + b.getTitle() + " | Year: " + b.getPublishedYear());
        } else {
            System.out.println("Book not found for ID: " + searchId);
        }

        // 3. findAll()
        System.out.println("All books with Author and Member info:");
        List<Book> books = bookService.findAll();
        for (Book b : books) {
            String authorName = (b.getAuthor() != null) ? b.getAuthor().getName() : "No Author";
            String memberName = (b.getBorrowedBy() != null) ? b.getBorrowedBy().getName() : "Not Borrowed";
            System.out.println("Book: " + b.getTitle() + " | Status: " + b.getStatus() + " | Author: " + authorName + " | Member: " + memberName);
        }
    }
}
