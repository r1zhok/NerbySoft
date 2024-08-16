package org.library.app.repository;

import org.library.app.entity.BookEntity;
import org.library.app.entity.MemberEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembersRepository extends CrudRepository<MemberEntity, Long> {

    boolean existsByName(String name);

    boolean existsByIdAndBorrowedBooksIsEmpty(Long id);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM BookEntity b JOIN b.members m " +
            "WHERE b.id = :bookId AND m.id = :memberId")
    boolean existsByBookIdAndMemberId(@Param("bookId") Long bookId, @Param("memberId") Long memberId);

    Optional<MemberEntity> findByName(String name);

    @Query("SELECT b FROM BookEntity b JOIN b.members m WHERE m.name = :memberName")
    List<BookEntity> findBooksBorrowedByMemberName(@Param("memberName") String memberName);
}
