package com.example.book.service.conf;

import com.example.book.service.model.Admin;
import com.example.book.service.model.enums.Role;
import com.example.book.service.repo.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * A data initializer that runs on application startup to ensure a default
 * administrator account exists in the database.
 * <p>
 * This component implements {@link CommandLineRunner}, which guarantees its {@code run}
 * method will be executed once the Spring application context has been fully loaded.
 * It is specifically activated for the "dev" profile, making it suitable for
 * development and testing environments without affecting production.
 * <p>
 * The initializer checks if a default admin user, defined in the application
 * properties, already exists. If not, it creates and saves a new admin user
 * with a securely encoded password. This ensures that the application is always
 * manageable from the start.
 *
 * @see CommandLineRunner
 * @see Profile
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * The email address for the default admin user.
     * Injected from the {@code app.admin.email} property in {@code application.properties}.
     */
    @Value("${app.admin.email}")
    private String adminEmail;

    /**
     * The raw (unencoded) password for the default admin user.
     * Injected from the {@code app.admin.password} property, which should ideally be
     * supplied via an environment variable for security.
     */
    @Value("${app.admin.password}")
    private String adminPassword;

    /**

     * The first name for the default admin user.
     * Injected from the {@code app.admin.firstName} property.
     */
    @Value("${app.admin.firstName}")
    private String adminFirstName;

    /**
     * The last name for the default admin user.
     * Injected from the {@code app.admin.lastName} property.
     */
    @Value("${app.admin.lastName}")
    private String adminLastName;

    /**
     * The main execution method that runs on application startup.
     * <p>
     * It first performs a safety check to ensure the admin password has been provided.
     * Then, it queries the database to see if the default admin user already exists.
     * If the user is not found, it proceeds to create a new {@link Admin} entity,
     * encodes its password, and saves it to the database.
     *
     * @param args incoming command line arguments (not used in this implementation).
     */
    @Override
    public void run(String... args) {
        // Safety check: Do not proceed if the password is not provided.
        if (adminPassword == null || adminPassword.isBlank()) {
            System.err.println("!!! WARNING: Default admin password is not set (check the ADMIN_PASSWORD environment variable). The admin user will not be created.");
            return;
        }

        // Check if the admin user already exists to prevent duplicates.
        if (adminRepository.findByEmail(adminEmail).isEmpty()) {
            // If not, build a new Admin entity.
            Admin admin = Admin.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword)) // Always encode passwords
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            // Save the new admin to the database.
            adminRepository.save(admin);
            System.out.println(">>> Created default admin user: " + adminEmail);
        } else {
            // If the user exists, simply log a message and do nothing.
            System.out.println(">>> Default admin user '" + adminEmail + "' already exists. Skipping initialization.");
        }
    }
}