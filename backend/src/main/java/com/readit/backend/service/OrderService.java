package com.readit.backend.service;

import com.readit.backend.dto.OrderDTO;
import com.readit.backend.dto.OrderItemDTO;
import com.readit.backend.dto.OrderRequest;
import com.readit.backend.entity.*;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.repository.BookRepository;
import com.readit.backend.repository.OrderRepository;
import com.readit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<OrderDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_IdOrderByOrderDateDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return toDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress() != null
                        ? request.getShippingAddress()
                        : user.getAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", itemReq.getBookId()));

            if (book.getInventory() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for book: " + book.getTitle());
            }

            // Decrement inventory
            book.setInventory(book.getInventory() - itemReq.getQuantity());
            bookRepository.save(book);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .book(book)
                    .quantity(itemReq.getQuantity())
                    .price(book.getPrice())
                    .build();

            order.getItems().add(item);
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    // -------------------- Mapping --------------------

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .bookId(item.getBook().getId())
                        .bookTitle(item.getBook().getTitle())
                        .bookImageUrl(item.getBook().getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .build();
    }
}
