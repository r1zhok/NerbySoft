package org.library.app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${member.max.book.limit}")
    private int bookLimit;

    private final MembersRepository repository;

    private final BooksRepository booksRepository;

    public List<MemberDTO> getAllMembers() {
        Iterable<MemberEntity> members = repository.findAll();
        return StreamSupport.stream(members.spliterator(), false)
                .map(member -> new MemberDTO(member.getName(), member.getCreationDate()))
                .collect(Collectors.toList());
    }

    public MemberDTO getMemberById(Long id) {
        return repository.findById(id)
                .map(member -> new MemberDTO(member.getName(), member.getCreationDate()))
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
    }

    public List<BookDTO> retrieveAllBookByMemberName(String name) {
        this.repository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Member by name not found"));
        return this.repository.findBooksBorrowedByMemberName(name)
                .stream().map(book -> new BookDTO(book.getTitle(), book.getAuthor(), book.getAmount())).toList();
    }

    @Transactional
    public MemberDTO createMember(NewMemberPayload memberDTO) {
        if (repository.existsByName(memberDTO.name())) {
            throw new MemberAlreadyExistException("Member already exists");
        }
        MemberEntity member = repository.save(
                new MemberEntity(null, memberDTO.name(), Date.valueOf(LocalDate.now()), new HashSet<>()));
        return new MemberDTO(member.getName(), member.getCreationDate());
    }

    @Transactional
    public void updateMember(NewMemberPayload payload, Long memberId) {
        repository.findById(memberId)
                .ifPresentOrElse(member -> {
                    if (!member.getName().equals(payload.name()) && repository.existsByName(payload.name())) {
                        throw new MemberAlreadyExistException("Member already exists");
                    }
                    member.setName(payload.name());
                    this.repository.save(member);
                }, () -> {
                    throw new NoSuchElementException("Member not found");
                });
    }

    @Transactional
    public void deleteMember(Long memberId) {
        repository.findById(memberId).orElseThrow(() -> new NoSuchElementException("Member not found"));
        if (this.repository.existsByIdAndBorrowedBooksIsEmpty(memberId)) {
            this.repository.deleteById(memberId);
        } else {
            throw new MemberHasBookException("Member has books");
        }
    }

    @Transactional
    public BookDTO memberBorrowBook(Long memberId, Long bookId) {
        if (this.repository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new MemberAlreadyHaveThisBookException("Member have this book");
        }
        MemberEntity memberEntity = this.repository
                .findById(memberId).orElseThrow(() -> new NoSuchElementException("Member not found"));

        if (memberEntity.getBorrowedBooks().size() >= bookLimit) {
            throw new MemberReachedLimitException("Limit of books is %s".formatted(bookLimit));
        }

        BookEntity bookEntity = this.booksRepository.findByIdAndAmountGreaterThan(bookId, 0)
                .orElseThrow(() -> new NoSuchElementException("Book not available"));

        bookEntity.setAmount(bookEntity.getAmount() - 1);
        bookEntity.getMembers().remove(memberEntity);
        this.booksRepository.save(bookEntity);

        memberEntity.getBorrowedBooks().add(bookEntity);
        this.repository.save(memberEntity);

        return new BookDTO(bookEntity.getTitle(), bookEntity.getAuthor(), bookEntity.getAmount());
    }

    @Transactional
    public void memberReturnBook(Long memberId, Long bookId) {
        MemberEntity memberEntity = this.repository
                .findById(memberId).orElseThrow(() -> new NoSuchElementException("Member not found"));
        BookEntity bookEntity = this.booksRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        bookEntity.setAmount(bookEntity.getAmount() + 1);
        bookEntity.getMembers().remove(memberEntity);
        this.booksRepository.save(bookEntity);

        memberEntity.getBorrowedBooks().remove(bookEntity);
        this.repository.save(memberEntity);
    }
}
