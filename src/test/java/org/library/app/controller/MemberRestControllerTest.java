package org.library.app.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.dto.MemberDTO;
import org.library.app.controller.payload.NewMemberPayload;
import org.library.app.exception.MemberAlreadyHaveThisBookException;
import org.library.app.exception.MemberHasBookException;
import org.library.app.exception.MemberReachedLimitException;
import org.library.app.service.MemberService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRestControllerTest {

    @Mock
    MemberService service;

    @InjectMocks
    MemberRestController controller;

    @Test
    void getMember_RequestIsValid_ReturnMember() {
        var member = new MemberDTO("Joshua Bloch", Date.valueOf(LocalDate.now()));

        doReturn(member).when(this.service).getMemberById(1L);
        var result = this.controller.getMember(1L);

        assertEquals(member, result.getBody());
    }

    @Test
    void getMember_RequestIsInvalid_ReturnNoSuchElementException() {
        doThrow(new NoSuchElementException("Member not found")).when(this.service).getMemberById(1L);
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.getMember(1L));
        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void updateMember_RequestIsValid_ReturnMember() throws BindException {
        var payload = new NewMemberPayload("Joshua Bloch");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        var result = this.controller.updateMember(1L, payload, bindingResult);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).updateMember(new NewMemberPayload("Joshua Bloch"),1L);
    }

    @Test
    void updateMember_RequestIsInvalid_ReturnsBadRequest() {
        var payload = new NewMemberPayload("Joshua ");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "name", "error"));

        var exception = assertThrows(BindException.class,
                () -> this.controller.updateMember(1L, payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "name", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.service);
    }

    @Test
    void updateBook_RequestIsInvalid_NoSuchElementException() {
        var payload = new NewMemberPayload("Joshua Bloch");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        doThrow(new NoSuchElementException("Member not found")).when(this.service).updateMember(payload,1L);
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.updateMember(1L, payload, bindingResult));
        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void deleteMember_ReturnsNoContent() {
        var result = this.controller.deleteMember(1L);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).deleteMember(1L);
    }

    @Test
    void deleteMember_RequestIsInvalid_NoSuchElementException() {
        doThrow(new NoSuchElementException("Member not found")).when(this.service).deleteMember(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.deleteMember(1L));

        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void deleteMember_RequestIsInvalid_MemberHasBookException() {
        doThrow(new MemberHasBookException("Member has books")).when(this.service).deleteMember(1L);

        var exception = assertThrows(MemberHasBookException.class,
                () -> this.controller.deleteMember(1L));

        assertEquals("Member has books", exception.getMessage());
    }

    @Test
    void memberBorrowBook_RequestIsValid_ReturnsBorrowedBook() {
        doReturn(new BookDTO("New book", "new author", 1))
                .when(this.service).memberBorrowBook(1L, 1L);

        var result = this.controller.memberBorrowBook(1L, 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(new BookDTO("New book", "new author", 1), result.getBody());

        verify(this.service).memberBorrowBook(1L, 1L);
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void memberBorrowBook_RequestIsInvalid_ReturnsNoSuchElementException() {
        doThrow(new NoSuchElementException("Member not found"))
                .when(this.service).memberBorrowBook(1L, 1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.memberBorrowBook(1L, 1L));

        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void memberBorrowBook_RequestIsInvalid_ReturnsMemberAlreadyHaveThisBookException() {
        doThrow(new MemberAlreadyHaveThisBookException("Member have this book"))
                .when(this.service).memberBorrowBook(1L, 1L);

        var exception = assertThrows(MemberAlreadyHaveThisBookException.class,
                () -> this.controller.memberBorrowBook(1L, 1L));

        assertEquals("Member have this book", exception.getMessage());
    }

    @Test
    void memberBorrowBook_RequestIsInvalid_ReturnsMemberReachedLimitException() {
        doThrow(new MemberReachedLimitException("Limit of books is 10"))
                .when(this.service).memberBorrowBook(1L, 1L);

        var exception = assertThrows(MemberReachedLimitException.class,
                () -> this.controller.memberBorrowBook(1L, 1L));

        assertEquals("Limit of books is 10", exception.getMessage());
    }

    @Test
    void memberReturnBook_RequestIsValid_ReturnsNoContent() {
        var result = this.controller.memberReturnBook(1L, 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.service).memberReturnBook(1L, 1L);
    }

    @Test
    void memberReturnBook_RequestIsValid_ReturnsNoSuchElementException() {
        doThrow(new NoSuchElementException("Member not found"))
                .when(this.service).memberReturnBook(1L, 1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.controller.memberReturnBook(1L, 1L));

        assertEquals("Member not found", exception.getMessage());
    }
}