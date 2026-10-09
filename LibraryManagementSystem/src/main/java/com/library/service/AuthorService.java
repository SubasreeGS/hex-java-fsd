package com.library.service;

import com.library.model.Author;
import com.library.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public void insert(String name, String country) {
        Author author = new Author();
        author.setName(name);
        author.setCountry(country);
        authorRepository.insert(author);
    }

    public Optional<Author> getAuthorById(Long authorId) {
        return authorRepository.getAuthorById(authorId);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.getAllAuthors();
    }
}
