package com.example.book.service.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookItemDTO {
    private Long id;
    private String name;
    private String author;
    private Integer quantity;
    private BigDecimal price;
}
