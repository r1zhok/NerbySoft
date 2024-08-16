package org.library.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.library.app.controller.dto.BookDTO;
import org.library.app.controller.dto.MemberDTO;
import org.library.app.controller.payload.NewMemberPayload;
import org.library.app.entity.BookEntity;
import org.library.app.entity.MemberEntity;
import org.library.app.exception.MemberAlreadyExistException;
import org.library.app.exception.MemberAlreadyHaveThisBookException;
import org.library.app.exception.MemberHasBookException;
import org.library.app.exception.MemberReachedLimitException;
import org.library.app.repository.BooksRepository;
import org.library.app.repository.MembersRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"member.max.book.limit=10"})
class MemberServiceTest {

    @Mock
    MembersRepository membersRepository;

    @Mock
    BooksRepository booksRepository;

    @Value("${member.max.book.limit}")
    int bookLimit = 10;

    @InjectMocks
    MemberService service;

    @Test
    void getAllMembers_ReturnAllMembers() {
        var members = IntStream.range(1, 4)
                .mapToObj(i ->
                        new MemberEntity(Integer.toUnsignedLong(i),
                                "Користувач №%d".formatted(i), Date.valueOf(LocalDate.now()) ,new HashSet<>())
                ).toList();
        doReturn(members).when(this.membersRepository).findAll();

        var result = service.getAllMembers();

        assertEquals(members.stream().map(member ->
                new MemberDTO(member.getName(), member.getCreationDate())).toList(), result);

        verify(this.membersRepository).findAll();
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void getMemberById_MemberExists_ReturnMember() {
        var member = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());
        doReturn(Optional.of(member)).when(membersRepository).findById(1L);

        var result = this.service.getMemberById(1L);

        assertNotNull(result);
        assertEquals(new MemberDTO(member.getName(), member.getCreationDate()), result);

        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void getMemberById_MemberDoesNotExists_ReturnsNoSuchElementException() {
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.getMemberById(1L));

        assertEquals("Member not found", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void retrieveAllBookByMemberName_NameCorrect_ReturnsAllBooks() {
        var members = List.of(new BookEntity(1L, "Книжка", "автор", 1, new HashSet<>()));
        doReturn(Optional.of(new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>())))
                .when(this.membersRepository).findByName("Member");
        doReturn(members)
                .when(this.membersRepository).findBooksBorrowedByMemberName("Member");

        var result = service.retrieveAllBookByMemberName("Member");

        assertNotNull(result);
        assertEquals(members.stream().map(book ->
                new BookDTO(book.getTitle(), book.getAuthor(), 1)).toList(), result);

        verify(this.membersRepository).findByName("Member");
        verify(this.membersRepository).findBooksBorrowedByMemberName("Member");
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void retrieveAllBookByMemberName_NameIsNotCorrect_ReturnsNoSuchElementException() {
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.retrieveAllBookByMemberName("Member"));

        assertEquals("Member by name not found", exception.getMessage());

        verify(this.membersRepository).findByName("Member");
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void createMember_RequestIsValid_ReturnsMember() {
        var payload = new NewMemberPayload("Member");
        var savedMember = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());

        doReturn(false).when(this.membersRepository).existsByName(payload.name());
        doReturn(savedMember).when(this.membersRepository).save(any(MemberEntity.class));

        var result = this.service.createMember(payload);

        assertNotNull(result);
        assertEquals(new MemberDTO(savedMember.getName(), savedMember.getCreationDate()), result);

        verify(this.membersRepository).save(any(MemberEntity.class));
        verify(this.membersRepository).existsByName("Member");
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void createMember_RequestIsInvalid_ReturnsMemberAlreadyExistException() {
        var payload = new NewMemberPayload("Member");
        doReturn(true).when(this.membersRepository).existsByName(payload.name());

        var exception = assertThrows(MemberAlreadyExistException.class,
                () -> this.service.createMember(new NewMemberPayload("Member")));

        assertEquals("Member already exists", exception.getMessage());

        verify(this.membersRepository).existsByName("Member");
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void updateMember_RequestIsValid_ReturnsMember() {
        var payload = new NewMemberPayload("Member1");
        var member = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());
        var savedMember = new MemberEntity(1L, "Member1", Date.valueOf(LocalDate.now()), new HashSet<>());

        doReturn(Optional.of(member)).when(this.membersRepository).findById(1L);
        doReturn(false).when(this.membersRepository).existsByName(payload.name());
        doReturn(savedMember).when(this.membersRepository).save(any(MemberEntity.class));

        this.service.updateMember(payload, 1L);

        verify(this.membersRepository).findById(1L);
        verify(this.membersRepository).existsByName(payload.name());
        verify(this.membersRepository).save(any(MemberEntity.class));
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void updateMember_RequestIsInvalid_ReturnsMemberAlreadyExistException() {
        var payload = new NewMemberPayload("Member2");
        var member = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());

        doReturn(Optional.of(member)).when(this.membersRepository).findById(1L);
        doReturn(true).when(this.membersRepository).existsByName(payload.name());

        var exception = assertThrows(MemberAlreadyExistException.class,
                () -> this.service.updateMember(payload, 1L));

        assertEquals("Member already exists", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verify(this.membersRepository).existsByName(payload.name());
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void updateMember_MemberNotFound_ThrowsNoSuchElementException() {
        var payload = new NewMemberPayload("Member1");
        doReturn(Optional.empty()).when(this.membersRepository).findById(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.updateMember(payload, 1L));

        assertEquals("Member not found", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void deleteMember_MemberExistsAndHasNoBooks_DeletesMember() {
        var member = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());
        doReturn(Optional.of(member)).when(this.membersRepository).findById(1L);
        doReturn(true).when(this.membersRepository).existsByIdAndBorrowedBooksIsEmpty(1L);

        this.service.deleteMember(1L);

        verify(this.membersRepository).findById(1L);
        verify(this.membersRepository).existsByIdAndBorrowedBooksIsEmpty(1L);
        verify(this.membersRepository).deleteById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }


    @Test
    void deleteMember_MemberNotFound_ThrowsNoSuchElementException() {
        doReturn(Optional.empty()).when(this.membersRepository).findById(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.deleteMember(1L));

        assertEquals("Member not found", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void deleteMember_MemberHasBooks_ThrowsMemberHasBookException() {
        var member = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());
        doReturn(Optional.of(member)).when(this.membersRepository).findById(1L);
        doReturn(false).when(this.membersRepository).existsByIdAndBorrowedBooksIsEmpty(1L);

        var exception = assertThrows(MemberHasBookException.class,
                () -> this.service.deleteMember(1L));

        assertEquals("Member has books", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verify(this.membersRepository).existsByIdAndBorrowedBooksIsEmpty(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void memberBorrowBook_MemberAlreadyHasBook_ThrowsMemberAlreadyHaveThisBookException() {
        doReturn(true).when(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);

        var exception = assertThrows(MemberAlreadyHaveThisBookException.class,
                () -> this.service.memberBorrowBook(1L, 1L));

        assertEquals("Member have this book", exception.getMessage());

        verify(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    @Test
    void memberBorrowBook_MemberNotFound_ThrowsNoSuchElementException() {
        doReturn(false).when(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        doReturn(Optional.empty()).when(this.membersRepository).findById(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.memberBorrowBook(1L, 1L));

        assertEquals("Member not found", exception.getMessage());

        verify(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }

    /*
    * Used reflection because bookLimit don't want set up
    * */
    @Test
    void memberBorrowBook_MemberReachedLimit_ThrowsMemberReachedLimitException()
            throws NoSuchFieldException, IllegalAccessException {
        Set<BookEntity> borrowedBooks = new HashSet<>();
        var bookLimitField = this.service.getClass().getDeclaredField("bookLimit");
        bookLimitField.setAccessible(true);
        bookLimitField.setInt(this.service, 10);
        for (int i = 1; i <= 10; i++) {
            borrowedBooks.add(new BookEntity((long) i, "Title " + i, "Author " + i, 1, new HashSet<>()));
        }
        MemberEntity memberEntity = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), borrowedBooks);

        doReturn(Optional.of(memberEntity)).when(this.membersRepository).findById(1L);

        var exception = assertThrows(MemberReachedLimitException.class,
                () -> this.service.memberBorrowBook(1L, 1L));

        assertEquals("Limit of books is 10", exception.getMessage());

        verify(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
    }


    @Test
    void memberBorrowBook_BookNotAvailable_ThrowsNoSuchElementException() throws NoSuchFieldException,
            IllegalAccessException {
        var bookLimitField = this.service.getClass().getDeclaredField("bookLimit");
        bookLimitField.setAccessible(true);
        bookLimitField.setInt(this.service, 10);
        var memberEntity = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());

        doReturn(false).when(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        doReturn(Optional.of(memberEntity)).when(this.membersRepository).findById(1L);
        doReturn(Optional.empty()).when(this.booksRepository).findByIdAndAmountGreaterThan(1L, 0);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.memberBorrowBook(1L, 1L));

        assertEquals("Book not available", exception.getMessage());

        verify(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        verify(this.membersRepository).findById(1L);
        verify(this.booksRepository).findByIdAndAmountGreaterThan(1L, 0);
        verifyNoMoreInteractions(this.membersRepository, this.booksRepository);
    }

    @Test
    void memberBorrowBook_BookBorrowedSuccessfully_ReturnsBookDTO()
            throws NoSuchFieldException, IllegalAccessException {
        var bookLimitField = this.service.getClass().getDeclaredField("bookLimit");
        bookLimitField.setAccessible(true);
        bookLimitField.setInt(this.service, 10);
        var memberEntity = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>());
        var bookEntity = new BookEntity(1L, "Title", "Author", 1, new HashSet<>());

        doReturn(false).when(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        doReturn(Optional.of(memberEntity)).when(this.membersRepository).findById(1L);
        doReturn(Optional.of(bookEntity)).when(this.booksRepository).findByIdAndAmountGreaterThan(1L, 0);

        var result = this.service.memberBorrowBook(1L, 1L);

        assertNotNull(result);
        assertEquals("Title", result.title());
        assertEquals("Author", result.author());
        assertEquals(0, result.amount());

        verify(this.membersRepository).existsByBookIdAndMemberId(1L, 1L);
        verify(this.membersRepository).findById(1L);
        verify(this.booksRepository).findByIdAndAmountGreaterThan(1L, 0);
        verify(this.booksRepository).save(bookEntity);
        verify(this.membersRepository).save(memberEntity);
        verifyNoMoreInteractions(this.membersRepository, this.booksRepository);
    }

    @Test
    void memberReturnBook_MemberNotFound_ThrowsNoSuchElementException() {
        doReturn(Optional.empty()).when(this.membersRepository).findById(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.memberReturnBook(1L, 1L));

        assertEquals("Member not found", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
        verifyNoMoreInteractions(this.booksRepository);
    }

    @Test
    void memberReturnBook_BookNotFound_ThrowsNoSuchElementException() {
        doReturn(Optional.of(new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>())))
                .when(this.membersRepository).findById(1L);
        doReturn(Optional.empty()).when(this.booksRepository).findById(1L);

        var exception = assertThrows(NoSuchElementException.class,
                () -> this.service.memberReturnBook(1L, 1L));

        assertEquals("Book not found", exception.getMessage());

        verify(this.membersRepository).findById(1L);
        verify(this.booksRepository).findById(1L);
        verifyNoMoreInteractions(this.membersRepository);
        verifyNoMoreInteractions(this.booksRepository);
    }

    @Test
    void memberReturnBook_SuccessfullyReturnsBook() {
        BookEntity bookEntity = new BookEntity(1L, "Book", "Author", 1, new HashSet<>());
        MemberEntity memberEntity = new MemberEntity(1L, "Member", Date.valueOf(LocalDate.now()), new HashSet<>(Set.of(bookEntity)));

        bookEntity.getMembers().add(memberEntity);

        doReturn(Optional.of(memberEntity)).when(this.membersRepository).findById(1L);
        doReturn(Optional.of(bookEntity)).when(this.booksRepository).findById(1L);

        this.service.memberReturnBook(1L, 1L);

        assertFalse(memberEntity.getBorrowedBooks().contains(bookEntity));
        assertFalse(bookEntity.getMembers().contains(memberEntity));
        assertEquals(2, bookEntity.getAmount());

        verify(this.membersRepository).findById(1L);
        verify(this.booksRepository).findById(1L);
        verify(this.booksRepository).save(bookEntity);
        verify(this.membersRepository).save(memberEntity);
        verifyNoMoreInteractions(this.membersRepository);
        verifyNoMoreInteractions(this.booksRepository);
    }
}