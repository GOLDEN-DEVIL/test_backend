package com.readit.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private String status;
    private String shippingName;
    private String shippingEmail;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingMethod;
    private String shippingNotes;
    private List<OrderItemDTO> items;
}
