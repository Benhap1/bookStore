package com.example.book.service.model;

import com.example.book.service.model.enums.AgeGroup;
import com.example.book.service.model.enums.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a book in the bookstore's catalog.
 * <p>
 * This entity contains all the descriptive and commercial information about a single
 * book, such as its title, author, price, and genre. Each instance of this class
 * corresponds to a single row in the {@code books} table.
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    /**
     * The unique identifier for the book.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The full name or title of the book.
     * This field is mandatory and must be unique across all books in the catalog.
     */
    @NotBlank(message = "Book name cannot be blank")
    @Size(max = 255, message = "Book name cannot exceed 255 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * The literary genre of the book (e.g., Fantasy, Sci-Fi, Mystery).
     */
    @NotBlank(message = "Genre cannot be blank")
    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    @Column(name = "genre", nullable = false, length = 100)
    private String genre;

    /**
     * The intended target age group for the book's content.
     * @see AgeGroup
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_age_group", length = 20)
    private AgeGroup targetAgeGroup;

    /**
     * The selling price of the book.
     * The value must be greater than zero.
     */
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * The original date when the book was published.
     * The date must be in the past or present.
     */
    @NotNull(message = "Publication date cannot be null")
    @PastOrPresent(message = "Publication date must be in the past or present")
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    /**
     * The name of the book's author.
     */
    @NotBlank(message = "Author cannot be blank")
    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    /**
     * The total number of pages in the book. Must be at least 1.
     */
    @NotNull(message = "Number of pages cannot be null")
    @Min(value = 1, message = "Number of pages must be at least 1")
    @Column(name = "pages", nullable = false)
    private Integer pages;

    /**
     * A text field for storing structured or key-value characteristics of the book.
     * For example: "Cover: Hardcover, Dimensions: 6x9 inches".
     * Mapped as a Large Object (LOB) to a TEXT column in the database.
     */
    @Lob
    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics;

    /**
     * A detailed description or synopsis of the book's content.
     * Mapped as a Large Object (LOB) to a TEXT column in the database.
     */
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * The language in which the book is written.
     * @see Language
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    private Language language;


    /**
     * Compares two Book objects for equality.
     * <p>
     * If both books have been persisted (i.e., their IDs are not null), equality is based on the ID.
     * If the books are new and have not been persisted, equality is based on their name.
     * This provides a robust equality check for both transient and persisted entities.
     *
     * @param o the object to compare against.
     * @return {@code true} if the objects are considered equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        // For persisted entities, ID is the most reliable identifier.
        if (id != null && book.id != null) {
            return Objects.equals(id, book.id);
        }
        // For transient (new) entities, fall back to the natural key (name).
        return Objects.equals(name, book.name);
    }

    /**
     * Generates a hash code for the Book.
     * <p>
     * The hash code is based on the ID if the entity is persisted, otherwise it's based
     * on the name. This is consistent with the {@link #equals(Object)} implementation.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : name);
    }
}