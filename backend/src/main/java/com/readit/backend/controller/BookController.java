package com.readit.backend.controller;

import com.readit.backend.dto.ApiResponse;
import com.readit.backend.dto.BookDTO;
import com.readit.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Book REST controller.
 * Demonstrates the separation of concerns — controller only handles
 * HTTP routing, delegating business logic entirely to BookService.
 *
 * GET endpoints are public; POST/PUT/DELETE require ADMIN role (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * GET /api/books?page=0&size=10&sort=title,asc
     * Returns paginated book listing wrapped in ApiResponse.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 12, sort = "title", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<BookDTO> books = bookService.getAllBooks(pageable, q, genre, language, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    /**
     * GET /api/books/{id}/related?limit=5
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getRelatedBooks(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getRelatedBooks(id, limit)));
    }

    /**
     * GET /api/books/search?q=harry&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
            @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<BookDTO> results = bookService.searchBooks(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * GET /api/books/category/{categoryId}?page=0&size=10
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<BookDTO> books = bookService.getBooksByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * POST /api/books  (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookDTO dto) {
        BookDTO created = bookService.createBook(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    /**
     * PUT /api/books/{id}  (ADMIN only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id, @Valid @RequestBody BookDTO dto) {
        BookDTO updated = bookService.updateBook(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Book updated", updated));
    }

    /**
     * DELETE /api/books/{id}  (ADMIN only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted", null));
    }
}
