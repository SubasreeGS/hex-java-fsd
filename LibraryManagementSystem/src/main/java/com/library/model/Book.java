package com.library.model;

import com.library.enums.BookStatus;
import com.library.enums.Genre;
import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne
    @JoinColumn(name = "borrowed_by_id", nullable = true)
    private Member borrowedBy;

    @Column(name = "published_year")
    private int publishedYear;

    public Book() {}

    public Book(String title, Genre genre, BookStatus status, Author author, int publishedYear) {
        this.title = title;
        this.genre = genre;
        this.status = status;
        this.author = author;
        this.publishedYear = publishedYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }

    public Member getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(Member borrowedBy) { this.borrowedBy = borrowedBy; }

    public int getPublishedYear() { return publishedYear; }
    public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre=" + genre +
                ", status=" + status +
                ", author=" + author +
                ", borrowedBy=" + borrowedBy +
                ", publishedYear=" + publishedYear +
                '}';
    }
}
