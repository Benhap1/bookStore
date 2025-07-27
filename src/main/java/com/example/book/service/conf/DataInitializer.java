package com.example.book.service.conf;


import com.example.book.service.model.Admin;
import com.example.book.service.model.enums.Role;
import com.example.book.service.repo.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.firstName}")
    private String adminFirstName;

    @Value("${app.admin.lastName}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            System.err.println("!!! WARNING: Default admin password is not set (check the ADMIN_PASSWORD environment variable). The admin user will not be created.");
            return;
        }

        if (adminRepository.findByEmail(adminEmail).isEmpty()) {
            Admin admin = Admin.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            adminRepository.save(admin);
            System.out.println(">>> Created default admin user: " + adminEmail);
        } else {
            System.out.println(">>> Default admin user '" + adminEmail + "' already exists. Skipping initialization.");
        }
    }
}
