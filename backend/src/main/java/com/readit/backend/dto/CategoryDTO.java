package com.readit.backend.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String img;
}
