package com.example.book.service.model;

import com.example.book.service.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents the base abstract entity for all users in the system.
 * <p>
 * This class defines the common attributes shared by all types of users,
 * such as login credentials (email and password), personal details, and role.
 * It implements Spring Security's {@link UserDetails} interface, making it
 * directly usable by the security framework for authentication and authorization.
 * <p>
 * The {@code InheritanceType.JOINED} strategy is used, meaning that common fields
 * are stored in the {@code users} table, while specific fields for subclasses
 * (like {@link Client} or {@link Admin}) are stored in their own separate tables,
 * linked by a foreign key.
 *
 * @see UserDetails
 * @see Client
 * @see Admin
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class User implements UserDetails {

    /**
     * The unique identifier for the user.
     * Generated automatically by the database upon creation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's first name.
     */
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * The user's last name.
     */
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * The user's email address. This serves as the unique username for authentication.
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * The user's securely hashed password.
     * This field should never store plain-text passwords.
     */
    @NotBlank(message = "Password cannot be blank")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * A flag indicating whether the user's account is enabled or disabled.
     * A disabled user cannot log in. This is used for blocking or deactivating accounts.
     * Defaults to {@code true}.
     */
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * The role assigned to the user, which determines their permissions within the application.
     * @see Role
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    /**
     * Compares two User objects for equality.
     * Two users are considered equal if they have the same ID and email.
     *
     * @param o the object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    /**
     * Generates a hash code for the User object based on its ID and email.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    /**
     * Returns the authorities granted to the user.
     * In this implementation, it's a single role prefixed with "ROLE_", as required by Spring Security.
     *
     * @return a collection containing the user's single granted authority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the username used to authenticate the user.
     * In this application, the email is used as the username.
     *
     * @return the user's email address.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Indicates whether the user's account has expired.
     * This implementation always returns {@code true}, meaning accounts never expire.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * This implementation always returns {@code true}, meaning accounts are never locked
     * through this mechanism. Account status is controlled by the {@link #enabled} field.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * This implementation always returns {@code true}, meaning credentials never expire.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * This method directly returns the value of the {@link #enabled} field.
     *
     * @return {@code true} if the user is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}