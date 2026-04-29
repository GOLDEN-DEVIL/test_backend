package com.readit.backend.repository;

import com.readit.backend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByCategory_Id(Long categoryId, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);

    Page<Book> findByGenreIgnoreCase(String genre, Pageable pageable);

    @Query("""
            SELECT b
            FROM Book b
            WHERE (:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))
              AND (:language IS NULL OR LOWER(b.language) LIKE LOWER(CONCAT('%', :language, '%')))
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            """)
    Page<Book> findByFilters(
            @Param("query") String query,
            @Param("genre") String genre,
            @Param("language") String language,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    List<Book> findByCategory_IdAndIdNot(Long categoryId, Long excludedBookId, Pageable pageable);

    List<Book> findByGenreIgnoreCaseAndIdNot(String genre, Long excludedBookId, Pageable pageable);
}
