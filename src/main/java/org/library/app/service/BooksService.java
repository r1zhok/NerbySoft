package org.library.app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.payload.NewBookPayload;
import org.library.app.entity.BookEntity;
import org.library.app.exception.BookIsOverException;
import org.library.app.repository.BooksRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class BooksService {

    private final BooksRepository repository;

    public List<BookDTO> getAllBooks() {
        Iterable<BookEntity> bookEntities = this.repository.findAll();
        return StreamSupport.stream(bookEntities.spliterator(), false)
                .map(entity -> new BookDTO(entity.getTitle(), entity.getAuthor(), entity.getAmount()))
                .collect(Collectors.toList());
    }

    public BookDTO getBookById(Long id) {
        return repository.findById(id)
                .map(entity ->
                        new BookDTO(entity.getTitle(), entity.getAuthor(), entity.getAmount())
                ).orElseThrow(() -> new NoSuchElementException("Book not found"));
    }

    @Transactional
    public BookDTO createBook(NewBookPayload bookPayload) {
        this.repository.findByTitleAndAuthor(bookPayload.title(), bookPayload.author())
                .ifPresentOrElse(bookEntity -> {
                            bookEntity.setAmount(bookEntity.getAmount() + 1);
                            repository.save(bookEntity);
                        }, () -> repository.save(new BookEntity(
                                null, bookPayload.title(), bookPayload.author(), 1, new HashSet<>())
                        )
                );

        return new BookDTO(bookPayload.title(), bookPayload.author(),
                this.repository.findByTitleAndAuthor(bookPayload.title(), bookPayload.author()).get().getAmount()
        );
    }

    @Transactional
    public void updateBook(Long id, NewBookPayload bookPayload) {
        this.repository.findById(id)
                .ifPresentOrElse(book -> {
                    book.setTitle(bookPayload.title());
                    book.setAuthor(bookPayload.author());
                    repository.save(book);
                }, () -> {
                    throw new NoSuchElementException("Book not found");
                });
    }

    @Transactional
    public void deleteBook(Long id) {
        this.repository.findById(id)
                .ifPresentOrElse(book -> {
                    if (book.getAmount() > 0) {
                        book.setAmount(book.getAmount() - 1);
                        repository.save(book);
                    } else {
                        throw new BookIsOverException("This book amount is over");
                    }
                }, () -> {
                    throw new NoSuchElementException("Book not found");
                });
    }

    public List<String> getAllDistinctBorrowedBooks() {
        return this.repository.findDistinctBorrowedBookTitles();
    }

    public List<String> getAllBorrowedBooksAndCountWasBorrowed() {
        return this.repository.findDistinctBorrowedBooksAndCounts()
                .stream().map(book -> "Book name: " + book[0].toString() + ", Book count that borrowed: " + book[1].toString())
                .toList();
    }
}
