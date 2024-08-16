package org.library.app.repository;

import org.library.app.entity.BookEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BooksRepository extends CrudRepository<BookEntity, Long> {

    Optional<BookEntity> findByTitleAndAuthor(String title, String author);

    Optional<BookEntity> findByIdAndAmountGreaterThan(Long id, Integer amount);

    @Query("SELECT DISTINCT b.title FROM BookEntity b JOIN b.members m")
    List<String> findDistinctBorrowedBookTitles();

    @Query("SELECT b.title AS bookTitle, COUNT(DISTINCT b.id) AS totalBorrowedTimes " +
            "FROM BookEntity b JOIN b.members m " +
            "GROUP BY b.title")
    List<Object[]> findDistinctBorrowedBooksAndCounts();
}
