package com.library.repository;

import com.library.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insert(Book book) {
        entityManager.persist(book);
    }

    public Optional<Book> findById(Long bookId) {
        return Optional
                .ofNullable(entityManager
                        .find(Book.class, bookId));
    }

    public List<Book> findAll() {
        String jpql = "select b from Book b join fetch b.author left join fetch b.borrowedBy";
        return entityManager
                .createQuery(jpql, Book.class)
                .getResultList();
    }
}