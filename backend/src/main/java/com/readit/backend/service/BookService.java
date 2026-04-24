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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return bookRepository.findAll(pageable)
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
        book.setLanguage(dto.getLanguage());
        book.setPrice(dto.getPrice());
        book.setImageUrl(dto.getImageUrl());
        book.setInventory(dto.getInventory() != null ? dto.getInventory() : 0);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            book.setCategory(category);
        }
    }
}
