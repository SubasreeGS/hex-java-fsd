package com.library.repository;

import com.library.model.Author;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insert(Author author) {
        entityManager.persist(author);
    }

    public Optional<Author> getAuthorById(Long authorId) {
        return Optional
                .ofNullable(entityManager
                        .find(Author.class, authorId));
    }

    public List<Author> getAllAuthors() {
        String jpql = "select a from Author a";
        return entityManager
                .createQuery(jpql, Author.class)
                .getResultList();
    }
}