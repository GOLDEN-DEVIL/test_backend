package com.readit.backend.service;

import com.readit.backend.dto.BookDTO;
import com.readit.backend.entity.Book;
import com.readit.backend.entity.Category;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.repository.BookRepository;
import com.readit.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for Book operations.
 * Demonstrates the Service layer pattern with ModelMapper DTO conversion.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    /**
     * Get all books with pagination.
     */
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return getAllBooks(pageable, null, null, null, null);
    }

    public Page<BookDTO> getAllBooks(
            Pageable pageable,
            String query,
            String genre,
            String language,
            BigDecimal maxPrice) {
        return bookRepository.findByFilters(
                        sanitize(query),
                        sanitize(genre),
                        sanitize(language),
                        maxPrice,
                        pageable)
                .map(this::toDTO);
    }

    /**
     * Get a single book by ID.
     */
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return toDTO(book);
    }

    /**
     * Search books by title or author (paginated).
     */
    public Page<BookDTO> searchBooks(String query, Pageable pageable) {
        return bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query, pageable)
                .map(this::toDTO);
    }

    /**
     * Get books by category (paginated).
     */
    public Page<BookDTO> getBooksByCategory(Long categoryId, Pageable pageable) {
        return bookRepository.findByCategory_Id(categoryId, pageable)
                .map(this::toDTO);
    }

    public List<BookDTO> getRelatedBooks(Long id, Integer limit) {
        Book current = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        int safeLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
        PageRequest page = PageRequest.of(0, safeLimit);

        Map<Long, Book> related = new LinkedHashMap<>();
        if (current.getCategory() != null) {
            bookRepository.findByCategory_IdAndIdNot(current.getCategory().getId(), id, page)
                    .forEach(book -> related.put(book.getId(), book));
        }

        if (related.size() < safeLimit && current.getGenre() != null) {
            bookRepository.findByGenreIgnoreCaseAndIdNot(current.getGenre(), id, page)
                    .forEach(book -> related.put(book.getId(), book));
        }

        return related.values().stream()
                .limit(safeLimit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new book (admin).
     */
    @Transactional
    public BookDTO createBook(BookDTO dto) {
        Book book = new Book();
        mapDtoToEntity(dto, book);
        Book saved = bookRepository.save(book);
        return toDTO(saved);
    }

    /**
     * Update an existing book (admin).
     */
    @Transactional
    public BookDTO updateBook(Long id, BookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        mapDtoToEntity(dto, book);
        Book updated = bookRepository.save(book);
        return toDTO(updated);
    }

    /**
     * Delete a book (admin).
     */
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book", "id", id);
        }
        bookRepository.deleteById(id);
    }

    // -------------------- Mapping Helpers --------------------

    private BookDTO toDTO(Book book) {
        BookDTO dto = modelMapper.map(book, BookDTO.class);
        dto.setCover(book.getImageUrl());
        dto.setLanguages(parseLanguages(book.getLanguage()));
        dto.setDeliveryDate("5-7 business days");
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }

    private void mapDtoToEntity(BookDTO dto, Book book) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setDescription(dto.getDescription());
        book.setGenre(dto.getGenre());
        String resolvedLanguage = sanitize(dto.getLanguage());
        if (resolvedLanguage == null && dto.getLanguages() != null && !dto.getLanguages().isEmpty()) {
            resolvedLanguage = dto.getLanguages().stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(", "));
        }
        book.setLanguage(resolvedLanguage);
        book.setPrice(dto.getPrice());
        String resolvedImageUrl = sanitize(dto.getImageUrl());
        if (resolvedImageUrl == null) {
            resolvedImageUrl = sanitize(dto.getCover());
        }
        book.setImageUrl(resolvedImageUrl);
        book.setInventory(dto.getInventory() != null ? dto.getInventory() : 0);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            book.setCategory(category);
        }
    }

    private List<String> parseLanguages(String language) {
        if (language == null || language.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(language.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
