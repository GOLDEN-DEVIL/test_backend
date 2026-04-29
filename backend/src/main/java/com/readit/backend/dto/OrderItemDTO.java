package com.readit.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String title;
    private String genre;
    private String bookImageUrl;
    private String image;
    private Integer quantity;
    private BigDecimal price;
}
