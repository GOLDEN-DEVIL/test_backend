package com.readit.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String title;
    private String genre;
    private String bookImageUrl;
    private String image;
    private BigDecimal bookPrice;
    private BigDecimal price;
    private Integer quantity;
}
