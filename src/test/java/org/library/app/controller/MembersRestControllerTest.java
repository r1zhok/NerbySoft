package org.library.app.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.dto.MemberDTO;
import org.library.app.controller.payload.NewMemberPayload;
import org.library.app.exception.MemberAlreadyExistException;
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
class MembersRestControllerTest {

    @Mock
    MemberService service;

    @InjectMocks
    MembersRestController controller;

    @Test
    void getAllMembers_ReturnsAllMembers() {
        doReturn(List.of(
                        new MemberDTO("Joshua Bloch", Date.valueOf(LocalDate.now())),
                        new MemberDTO("Robert C. Martin", Date.valueOf(LocalDate.now())),
                        new MemberDTO("Craig Walls", Date.valueOf(LocalDate.now()))
                )
        ).when(this.service).getAllMembers();

        var result = this.controller.getAllMembers();

        assertEquals(List.of(
                new MemberDTO("Joshua Bloch", Date.valueOf(LocalDate.now())),
                new MemberDTO("Robert C. Martin", Date.valueOf(LocalDate.now())),
                new MemberDTO("Craig Walls", Date.valueOf(LocalDate.now()))
        ), result.getBody());
    }

    @Test
    void getAllBooksByMemberName_RequestIsValid_ReturnsAllBooks() {
        doReturn(List.of(
                        new BookDTO("Effective Java", "Joshua Bloch", 5),
                        new BookDTO("Clean Code", "Robert C. Martin", 3),
                        new BookDTO("Spring in Action", "Craig Walls", 7)
                )
        ).when(this.service).retrieveAllBookByMemberName("jon");

        var result = this.controller.getAllBooksByMemberName("jon");

        assertEquals(List.of(
                new BookDTO("Effective Java", "Joshua Bloch", 5),
                new BookDTO("Clean Code", "Robert C. Martin", 3),
                new BookDTO("Spring in Action", "Craig Walls", 7)
        ), result.getBody());
    }

    @Test
    void getAllBooksByMemberName_RequestIsInvalid_ReturnsNoSuchElementException() {
        doThrow(new NoSuchElementException("Member not found")).when(this.service).retrieveAllBookByMemberName("jon");
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.getAllBooksByMemberName("jon"));
        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void createMember_RequestIsValid_ReturnsMember() throws BindException {
        var payload = new NewMemberPayload("New member");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        doReturn(new MemberDTO("New book", Date.valueOf(LocalDate.now())))
                .when(this.service).createMember(payload);

        var result = this.controller.createMember(payload, bindingResult);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(new MemberDTO("New book", Date.valueOf(LocalDate.now())), result.getBody());

        verify(this.service).createMember(payload);
        verifyNoMoreInteractions(this.service);
    }

    @Test
    void createMember_RequestIsInvalid_ReturnsMemberAlreadyExistException() {
        var payload = new NewMemberPayload("New member");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        doThrow(new MemberAlreadyExistException("Member already exists"))
                .when(this.service).createMember(payload);
        var exception = assertThrows(MemberAlreadyExistException.class,
                () -> this.controller.createMember(payload, bindingResult));
        assertEquals("Member already exists", exception.getMessage());
    }

    @Test
    void createMember_RequestIsInvalid_ReturnsReturnsBadRequest() {
        var payload = new NewMemberPayload("   ");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "name", "error"));

        var exception = assertThrows(BindException.class,
                () -> this.controller.createMember(payload, bindingResult));

        assertEquals(List.of(new FieldError("payload", "name", "error"))
                , exception.getAllErrors());
        verifyNoInteractions(this.service);
    }
}