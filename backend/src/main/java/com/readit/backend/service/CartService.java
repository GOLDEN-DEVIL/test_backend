package com.readit.backend.service;

import com.readit.backend.dto.CartItemDTO;
import com.readit.backend.dto.CartItemRequest;
import com.readit.backend.entity.Book;
import com.readit.backend.entity.CartItem;
import com.readit.backend.entity.User;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.repository.BookRepository;
import com.readit.backend.repository.CartItemRepository;
import com.readit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<CartItemDTO> getCartByUser(Long userId) {
        return cartItemRepository.findByUser_Id(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDTO addToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        // If item already in cart, update quantity
        CartItem cartItem = cartItemRepository
                .findByUser_IdAndBook_Id(userId, request.getBookId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    return existing;
                })
                .orElse(CartItem.builder()
                        .user(user)
                        .book(book)
                        .quantity(request.getQuantity())
                        .build());

        CartItem saved = cartItemRepository.save(cartItem);
        return toDTO(saved);
    }

    @Transactional
    public CartItemDTO updateCartItem(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
        item.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(item);
        return toDTO(updated);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new ResourceNotFoundException("CartItem", "id", cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteAllByUser_Id(userId);
    }

    private CartItemDTO toDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .bookId(item.getBook().getId())
                .bookTitle(item.getBook().getTitle())
                .title(item.getBook().getTitle())
                .genre(item.getBook().getGenre())
                .bookImageUrl(item.getBook().getImageUrl())
                .image(item.getBook().getImageUrl())
                .bookPrice(item.getBook().getPrice())
                .price(item.getBook().getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
