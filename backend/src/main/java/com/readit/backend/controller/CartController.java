package com.readit.backend.controller;

import com.readit.backend.dto.ApiResponse;
import com.readit.backend.dto.CartItemDTO;
import com.readit.backend.dto.CartItemQuantityUpdateRequest;
import com.readit.backend.dto.CartItemRequest;
import com.readit.backend.service.CartService;
import com.readit.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(resolveUsername(userDetails)).getId();
        return ResponseEntity.ok(ApiResponse.success(cartService.getCartByUser(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = userService.getUserByUsername(resolveUsername(userDetails)).getId();
        CartItemDTO item = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @PathVariable Long id,
            @Valid @RequestBody CartItemQuantityUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateCartItem(id, request.getQuantity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(resolveUsername(userDetails)).getId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    private String resolveUsername(UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : "demo";
    }
}
