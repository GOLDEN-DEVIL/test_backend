package com.readit.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String genre;
    private String language;
    private BigDecimal price;
    private String imageUrl;
    private Integer inventory;
    private Long categoryId;
    private String categoryName;
}
