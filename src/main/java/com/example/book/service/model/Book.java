package com.example.book.service.model;

import com.example.book.service.model.enums.AgeGroup;
import com.example.book.service.model.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book name cannot be blank")
    @Size(max = 255, message = "Book name cannot exceed 255 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Genre cannot be blank")
    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    @Column(name = "genre", nullable = false, length = 100)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_age_group", length = 20)
    private AgeGroup targetAgeGroup;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Publication date cannot be null")
    @PastOrPresent(message = "Publication date must be in the past or present")
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @NotBlank(message = "Author cannot be blank")
    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @NotNull(message = "Number of pages cannot be null")
    @Min(value = 1, message = "Number of pages must be at least 1")
    @Column(name = "pages", nullable = false)
    private Integer pages;

    @Lob
    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    private Language language;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        if (id != null && book.id != null) {
            return Objects.equals(id, book.id);
        }
        return Objects.equals(name, book.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : name);
    }
}