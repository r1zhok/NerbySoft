package org.library.app.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.payload.NewBookPayload;
import org.library.app.exception.BookIsOverException;
import org.library.app.service.BooksService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BooksRestControllerTest {

    @Mock
    BooksService service;

    @InjectMocks
    BooksRestController controller;

    @Test
    void getAllBooks_ReturnsBookList() {
        doReturn(List.of(
                        new BookDTO("Effective Java", "Joshua Bloch", 5),
                        new BookDTO("Clean Code", "Robert C. Martin", 3),
                        new BookDTO("Spring in Action", "Craig Walls", 7)
                )
        ).when(this.service).getAllBooks();

        var result = this.controller.getAllBooks();

        assertEquals(List.of(
                new BookDTO("Effective Java", "Joshua Bloch", 5),
                new BookDTO("Clean Code", "Robert C. Martin", 3),
                new BookDTO("Spring in Action", "Craig Walls", 7)
        ), result.getBody());
    }

    @Test
    void getBookById_BookExists_ReturnsBook() {
        var book = new BookDTO("Effective Java", "Joshua Bloch", 5);

        doReturn(book).when(this.service).getBookById(1L);
        var result = this.controller.getBookById(1L);

        assertEquals(book, result.getBody());
    }

    @Test
    void getBookById_BookDoesNotExists_NoSuchElementException() {
        doThrow(new NoSuchElementException("Book not found")).when(this.service).getBookById(1L);
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.getBookById(1L));
        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void getAllBorrowedBooksAndCountWasBorrowed_ReturnsListOfStrings() {
        doReturn(List.of(
                "Book name: Effective Java, Book count that borrowed: 3"
        )).when(this.service).getAllBorrowedBooksAndCountWasBorrowed();

        var result = this.controller.getAllBorrowedBooksAndCountWasBorrowed();

        assertEquals(List.of(
                "Book name: Effective Java, Book count that borrowed: 3"
        ), result.getBody());
    }

    @Test
    void getAllDistinctBorrowedBooks_ReturnsListOfStrings() {
        doReturn(List.of(
                "Effective Java", "Joshua Bloch", "Robert C. Martin"
        )).when(this.service).getAllDistinctBorrowedBooks();

        var result = this.controller.getAllDistinctBorrowedBooks();

        assertEquals(List.of(
                "Effective Java", "Joshua Bloch", "Robert C. Martin"
        ), result.getBody());
    }

    @Test
    void createBook_RequestIsValid_ReturnsBook() throws BindException {
        var payload = new NewBookPayload("New book", "new author");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        doReturn(new BookDTO("New book", "new author", 1))
                .when(this.service).createBook(payload);

        var result = this.controller.createBook(payload, bindingResult);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(new BookDTO("New book", "new author", 1), result.getBody());

        verify(this.service).createBook(payload);
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void createBook_RequestIsInvalid_ReturnsBadRequest() {
        var payload = new NewBookPayload("New book", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "author", "error"));

        var exception = assertThrows(BindException.class,
                () -> this.controller.createBook(payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "author", "error"))
                , exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void updateBook_RequestIsValid_ReturnsNoContent() throws BindException {
        var payload = new NewBookPayload("New name", "New author");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        var result = this.controller.updateBook(1L, payload, bindingResult);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).updateBook(1L, new NewBookPayload("New name", "New author"));
    }

    @Test
    void updateBook_RequestIsInvalid_ReturnsBadRequest() {
        var payload = new NewBookPayload("New name", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "author", "error"));

        var exception = assertThrows(BindException.class, () -> this.controller.updateBook(1L, payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "author", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void updateBook_RequestIsInvalid_NoSuchElementException() {
        var payload = new NewBookPayload("New name", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        doThrow(new NoSuchElementException("Book not found")).when(this.service).updateBook(1L, payload);
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.updateBook(1L, payload, bindingResult));
        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void deleteBook_ReturnsNoContent() {
        var result = this.controller.deleteBook(1L);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).deleteBook(1L);
    }

    @Test
    void deleteBook_BookDoesNotExist_NoSuchElementException() {
        doThrow(new NoSuchElementException("Book not found")).when(this.service).deleteBook(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.deleteBook(1L));

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void deleteBook_AmountEqualsZero_BookIsOverException() {
        doThrow(new BookIsOverException("This book amount is over")).when(this.service).deleteBook(1L);

        var exception = assertThrows(BookIsOverException.class,
                () -> this.controller.deleteBook(1L));

        assertEquals("This book amount is over", exception.getMessage());
    }
}