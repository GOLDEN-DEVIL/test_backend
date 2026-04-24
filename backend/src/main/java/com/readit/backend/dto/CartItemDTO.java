package com.readit.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookImageUrl;
    private BigDecimal bookPrice;
    private Integer quantity;
}
