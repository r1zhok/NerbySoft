package org.library.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.payload.NewBookPayload;
import org.library.app.entity.BookEntity;
import org.library.app.exception.BookIsOverException;
import org.library.app.repository.BooksRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BooksServiceTest {

    @Mock
    BooksRepository repository;

    @InjectMocks
    BooksService service;

    @Test
    void getAllBooks_ReturnsAllBooks() {
        var books = IntStream.range(1, 4)
                .mapToObj(i ->
                        new BookEntity(Integer.toUnsignedLong(i),
                                "Книжка №%d".formatted(i), "Автор №%d".formatted(i), i, new HashSet<>())
                ).toList();

        doReturn(books).when(this.repository).findAll();

        var result = this.service.getAllBooks();

        assertEquals(books.stream().map(
                book -> new BookDTO(book.getTitle(), book.getAuthor(), book.getAmount())).toList(), result);

        verify(this.repository).findAll();
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void getBookById_BookExists_ReturnsBook() {
        var book = new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>());
        doReturn(Optional.of(book)).when(repository).findById(1L);

        var result = this.service.getBookById(1L);

        assertNotNull(result);
        assertEquals(new BookDTO(book.getTitle(), book.getAuthor(), book.getAmount()), result);

        verify(this.repository).findById(1L);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void getBookById_BookDoesNotExist_ReturnsNoSuchElementException() {
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.getBookById(1L));

        assertEquals("Book not found", exception.getMessage());

        verify(this.repository).findById(1L);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void createBook_ReturnsCreatedBook() {
        var newBook = new NewBookPayload("Книжка", "автор");
        doReturn(new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>()))
                .when(this.repository).save(
                        new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>())
                );
        doReturn(Optional.of(new BookEntity(1L, "Книжка", "автор", 0, new HashSet<>())))
                .when(this.repository)
                .findByTitleAndAuthor(newBook.title(), newBook.author());

        var result = this.service.createBook(newBook);

        assertEquals(new BookDTO("Книжка", "автор", 1), result);

        verify(this.repository).save(new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>()));
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void createBook_BookIsCreated_ReturnsUpdatedBook() {
        var newBook = new NewBookPayload("Книжка", "автор");
        doReturn(new BookEntity(1L, "Книжка", "автор", 2, new HashSet<>()))
                .when(this.repository).save(
                        new BookEntity(1L, "Книжка", "автор", 2, new HashSet<>())
                );
        doReturn(Optional.of(new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>())))
                .when(this.repository)
                .findByTitleAndAuthor(newBook.title(), newBook.author());

        var result = this.service.createBook(newBook);

        assertEquals(new BookDTO("Книжка", "автор", 2), result);
    }

    @Test
    void updateBook_BookExists_ReturnsUpdatedBook() {
        var book = new NewBookPayload("Книжка", "автор");
        var bookEntity = new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>());
        doReturn(Optional.of(bookEntity))
                .when(this.repository).findById(1L);

        this.service.updateBook(1L, book);

        verify(this.repository).findById(1L);
        verify(this.repository).save(bookEntity);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void updateBook_BookDoesNotExists_ReturnsUpdatedBook() {
        assertThrows(NoSuchElementException.class, () -> this.service
                .updateBook(1L, new NewBookPayload("Книжка", "автор")));


        verify(this.repository).findById(1L);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void deleteProduct_BookExists() {
        var bookEntity = new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>());
        doReturn(Optional.of(bookEntity))
                .when(this.repository).findById(1L);
        doReturn(new BookEntity(1L, "Книжка", "автор", 0, new HashSet<>()))
                .when(this.repository).save(bookEntity);

        this.service.deleteBook(1L);

        verify(this.repository).findById(1L);
        verify(this.repository).save(bookEntity);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void deleteProduct_BookAmountIsZero_ReturnsBookIsOverException() {
        var bookEntity = new BookEntity(1L, "Книжка", "автор", 0, new HashSet<>());
        doReturn(Optional.of(bookEntity)).when(this.repository).findById(1L);

        assertThrows(BookIsOverException.class, () -> this.service.deleteBook(1L));

        verify(this.repository).findById(1L);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void deleteProduct_BookDoesNotExists_ReturnsNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> this.service
                .deleteBook(1L));


        verify(this.repository).findById(1L);
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void getAllDistinctBorrowedBooks_ReturnsDistinctBooks() {
        var books = List.of("Book 1", "Book 2", "Book 3", "Book 4", "Book 5");
        doReturn(books).when(this.repository).findDistinctBorrowedBookTitles();

        var result = this.service.getAllDistinctBorrowedBooks();

        assertNotNull(result);
        assertEquals(books, result);

        verify(this.repository).findDistinctBorrowedBookTitles();
        verifyNoMoreInteractions(this.repository);
    }

    @Test
    void getAllBorrowedBooksAndCountWasBorrowed_ReturnsBooks() {
        Object[] book1 = new Object[]{"Книжка1", 5};
        Object[] book2 = new Object[]{"Книжка2", 3};

        doReturn(List.of(book1, book2))
                .when(repository).findDistinctBorrowedBooksAndCounts();

        List<String> result = service.getAllBorrowedBooksAndCountWasBorrowed();

        assertEquals(2, result.size());
        assertEquals("Book name: Книжка1, Book count that borrowed: 5", result.get(0));
        assertEquals("Book name: Книжка2, Book count that borrowed: 3", result.get(1));

        verify(this.repository).findDistinctBorrowedBooksAndCounts();
        verifyNoMoreInteractions(this.repository);
    }
}