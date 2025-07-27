package com.example.book.service.dto;

import com.example.book.service.model.enums.AgeGroup;
import com.example.book.service.model.enums.Language;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {
    private Long id;
    private String name;
    private String genre;
    private AgeGroup targetAgeGroup;
    private BigDecimal price;
    private LocalDate publicationDate;
    private String author;
    private Integer pages;
    private String characteristics;
    private String description;
    private Language language;
}

