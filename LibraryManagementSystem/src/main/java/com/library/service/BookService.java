package com.library.service;

import com.library.enums.BookStatus;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final MemberRepository memberRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void save(Long authorId, Long memberId, Book book) {
        // Step 1: Fetch Author object from DB using authorId : validate authorId
        Optional<Author> optionalAuthor = authorRepository.getAuthorById(authorId);
        if (optionalAuthor.isEmpty())
            throw new RuntimeException("AuthorId is invalid");

        Author author = optionalAuthor.get();

        // Step 2: Fetch Member if present
        Member member = null;
        if (memberId != null) {
            Optional<Member> optionalMember = memberRepository.getMemberById(memberId);
            if (optionalMember.isEmpty())
                throw new RuntimeException("MemberId is invalid");

            member = optionalMember.get();
        }

        // Step 3: Attach author and member to book
        book.setAuthor(author);
        book.setBorrowedBy(member);

        // Step 4: Pass book to BookRepository
        bookRepository.insert(book);
    }

    public Optional<Book> findById(Long bookId) {
        return bookRepository.findById(bookId);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }
}
