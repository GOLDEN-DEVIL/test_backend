package com.readit.backend.controller;

import com.readit.backend.dto.ApiResponse;
import com.readit.backend.dto.OrderDTO;
import com.readit.backend.dto.OrderRequest;
import com.readit.backend.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * GET /api/orders — get current user's orders.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUser(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    /**
     * POST /api/orders — place a new order for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        OrderDTO created = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", created));
    }
}
