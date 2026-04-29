package com.readit.backend.service;

import com.readit.backend.dto.OrderDTO;
import com.readit.backend.dto.OrderItemDTO;
import com.readit.backend.dto.OrderRequest;
import com.readit.backend.entity.*;
import com.readit.backend.exception.InsufficientStockException;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.exception.UnauthorizedAccessException;
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

    public OrderDTO getOrderByIdForUser(Long userId, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Order", "id", id);
        }
        return toDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Order order = Order.builder()
                .user(user)
                .shippingName(buildShippingName(request))
                .shippingEmail(request.getShippingDetails() != null ? request.getShippingDetails().getEmail() : user.getEmail())
                .shippingPhone(request.getShippingDetails() != null ? request.getShippingDetails().getPhone() : user.getPhone())
                .shippingCity(request.getShippingDetails() != null ? request.getShippingDetails().getCity() : null)
                .shippingState(request.getShippingDetails() != null ? request.getShippingDetails().getState() : null)
                .shippingZipCode(request.getShippingDetails() != null ? request.getShippingDetails().getZipCode() : null)
                .shippingNotes(request.getShippingDetails() != null ? request.getShippingDetails().getDescription() : null)
                .shippingMethod(request.getShippingDetails() != null ? request.getShippingDetails().getShippingMethod() : null)
                .shippingAddress(resolveShippingAddress(request, user))
                .totalAmount(BigDecimal.ZERO)
                .shippingCost(resolveShippingCost(request))
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", itemReq.getBookId()));

            if (book.getInventory() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        book.getTitle(), itemReq.getQuantity(), book.getInventory());
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

        order.setTotalAmount(total.add(order.getShippingCost()));
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
                        .title(item.getBook().getTitle())
                        .genre(item.getBook().getGenre())
                        .bookImageUrl(item.getBook().getImageUrl())
                        .image(item.getBook().getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .shippingCost(order.getShippingCost())
                .status(order.getStatus())
                .shippingName(order.getShippingName())
                .shippingEmail(order.getShippingEmail())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZipCode(order.getShippingZipCode())
                .shippingMethod(order.getShippingMethod())
                .shippingNotes(order.getShippingNotes())
                .items(items)
                .build();
    }

    private String resolveShippingAddress(OrderRequest request, User user) {
        if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            return request.getShippingAddress().trim();
        }
        if (request.getShippingDetails() != null) {
            OrderRequest.ShippingDetailsRequest shipping = request.getShippingDetails();
            return String.format("%s, %s, %s",
                    shipping.getCity(),
                    shipping.getState(),
                    shipping.getZipCode());
        }
        return user.getAddress();
    }

    private String buildShippingName(OrderRequest request) {
        if (request.getShippingDetails() == null) {
            return null;
        }
        return (request.getShippingDetails().getFirstName() + " "
                + request.getShippingDetails().getLastName()).trim();
    }

    private BigDecimal resolveShippingCost(OrderRequest request) {
        if (request.getShippingDetails() == null || request.getShippingDetails().getShippingMethod() == null) {
            return BigDecimal.ZERO;
        }
        String method = request.getShippingDetails().getShippingMethod().trim().toLowerCase();
        if ("express".equals(method)) {
            return BigDecimal.valueOf(500);
        }
        return BigDecimal.ZERO;
    }
}
