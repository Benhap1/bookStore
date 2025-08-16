package com.example.book.service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents an Administrator user in the system.
 * <p>
 * An {@code Admin} is a specialized type of {@link User} with elevated privileges,
 * such as managing the book catalog, viewing all orders, and managing client accounts.
 * <p>
 * This class is currently a marker entity; it inherits all its fields and behavior
 * from the {@link User} superclass and does not add any new properties of its own.
 * Its primary purpose is to provide a distinct type that can be used for authorization
 * checks (e.g., {@code @PreAuthorize("hasRole('ADMIN')")}) and to be mapped to the
 * separate {@code admins} table in the database.
 * <p>
 * This class uses the {@code @PrimaryKeyJoinColumn} annotation, which is part of the
 * JOINED inheritance strategy defined in the {@code User} superclass.
 *
 * @see User
 */
@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id") // Specifies the link to the parent 'users' table
@NoArgsConstructor
@SuperBuilder // Allows building an instance including fields from the superclass
public class Admin extends User {
    // This entity currently has no additional fields.
    // Its existence allows for clear role separation and type safety.
}
