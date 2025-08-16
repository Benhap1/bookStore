package com.example.book.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

/**
 * Represents a line item within an {@link Order}.
 * <p>
 * This entity acts as a join table between the {@code orders} and {@code books} tables,
 * but with an additional column for {@code quantity}. It specifies how many copies of a
 * particular {@link Book} are included in a specific {@link Order}. Each instance of
 * this class corresponds to a single row in the {@code order_items} table.
 *
 * @see Order
 * @see Book
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookItem {

    /**
     * The unique identifier for the order item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent {@link Order} to which this line item belongs.
     * <p>
     * This is a many-to-one relationship, as an order can have many items, but each item
     * belongs to exactly one order.
     * <p>
     * {@code FetchType.LAZY} is used for performance optimization, ensuring that the
     * parent Order is not loaded from the database unless it's explicitly accessed.
     * <p>
     * {@code @ToString.Exclude} and {@code @EqualsAndHashCode.Exclude} are crucial to prevent
     * infinite loops and stack overflow errors when generating these methods, as {@code Order}
     * also has a reference back to this class.
     */
    @NotNull(message = "Order cannot be null for a book item")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    /**
     * The {@link Book} associated with this line item.
     * <p>
     * This is a many-to-one relationship, as a book can appear in many different order items,
     * but each order item refers to exactly one book.
     * <p>
     * {@code FetchType.LAZY} is used to avoid unnecessary loading of the full Book entity
     * when only the order item details are needed.
     */
    @NotNull(message = "Book cannot be null for a book item")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Book book;

    /**
     * The number of copies of the associated {@link Book} in this order item.
     * The value must be at least 1.
     */
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Compares two BookItem objects for equality.
     * They are considered equal if they have the same ID. This is a common and reliable
     * strategy for JPA entities once they have been persisted.
     *
     * @param o the object to compare against.
     * @return {@code true} if the objects have the same ID, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookItem bookItem = (BookItem) o;
        return Objects.equals(id, bookItem.id);
    }

    /**
     * Generates a hash code for the BookItem based on its ID.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}