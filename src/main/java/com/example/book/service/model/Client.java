package com.example.book.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Represents a Client user in the system.
 * <p>
 * A {@code Client} is a specialized type of {@link User} who has the ability to
 * browse books, place orders, and manage a personal balance. This entity extends
 * the common user attributes from the {@code User} class and adds client-specific
 * properties.
 * <p>
 * This class uses the {@code @PrimaryKeyJoinColumn} annotation, which is part of the
 * JOINED inheritance strategy defined in the {@code User} superclass. This means
 * that the primary key of the {@code clients} table is also a foreign key
 * referencing the {@code users} table.
 *
 * @see User
 * @see Order
 */
@Entity
@Table(name = "clients")
@PrimaryKeyJoinColumn(name = "user_id") // Specifies the link to the parent 'users' table
@Getter
@Setter
@EqualsAndHashCode(callSuper = true) // Includes superclass fields in equals() and hashCode()
@ToString(callSuper = true) // Includes superclass fields in toString()
@NoArgsConstructor
@SuperBuilder // Allows building an instance including fields from the superclass
public class Client extends User {

    /**
     * The monetary balance available to the client for making purchases.
     * <p>
     * This field is non-nullable and defaults to {@code BigDecimal.ZERO} for all
     * new clients upon creation. It is updated when a client tops up their account
     * or when an order is successfully submitted.
     */
    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
