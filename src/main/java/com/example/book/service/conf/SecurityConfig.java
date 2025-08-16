package com.example.book.service.conf;

import com.example.book.service.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
/**
 * Main configuration class for Spring Security.
 * <p>
 * This class defines the application's security policies, including URL-based authorization,
 * form-based login, logout handling, and the password encoding strategy. It integrates
 * custom components like {@link AppUserDetailsService} and {@link CustomAuthenticationFailureHandler}
 * to tailor the authentication process to the application's needs.
 *
 * @see EnableWebSecurity marks this class as a source of web security configuration.
 * @see EnableMethodSecurity enables method-level security annotations like {@code @PreAuthorize}.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Custom service for loading user-specific data from the database.
     */
    private final AppUserDetailsService userDetailsService;
    /**
     * Custom handler for processing failed login attempts.
     */
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    /**
     * Defines the primary security filter chain that governs all HTTP requests.
     * <p>
     * This bean configures the core security rules:
     * <ul>
     *   <li><b>CSRF Protection:</b> Disabled for simplicity in this application context.</li>
     *   <li><b>Authorization Rules:</b> Specifies which URL patterns are public and which require authentication.
     *       Static resources, the home page, public book listings, and authentication pages are publicly accessible.
     *       All other requests must be authenticated.</li>
     *   <li><b>UserDetailsService:</b> Registers our custom {@code AppUserDetailsService} to fetch user data.</li>
     *   <li><b>Form Login:</b> Configures a custom login page, a URL for processing credentials, and success/failure handling.
     *       It uses our {@code CustomAuthenticationFailureHandler} for detailed error feedback.</li>
     *   <li><b>Logout:</b> Configures the URL to trigger a logout and the page to redirect to afterward.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object to configure.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable Cross-Site Request Forgery protection.
                .csrf(AbstractHttpConfigurer::disable)
                // Define authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to static resources and public pages.
                        .requestMatchers(
                                "/", "/home", "/css/**", "/js/**", "/images/**",
                                "/books/list", "/books/view/**"
                        ).permitAll()
                        // Allow unauthenticated access to all authentication-related pages.
                        .requestMatchers("/auth/**").permitAll()
                        // All other requests must be authenticated.
                        .anyRequest().authenticated()
                )
                // Register the service that loads user data.
                .userDetailsService(userDetailsService)
                // Configure form-based authentication.
                .formLogin(form -> form
                        .loginPage("/auth/login") // The URL of our custom login page.
                        .loginProcessingUrl("/auth/login") // The URL where login credentials are POSTed.
                        .defaultSuccessUrl("/books/list", true) // Where to redirect after a successful login.
                        .failureHandler(customAuthenticationFailureHandler) // Use our custom handler for login errors.
                        .permitAll() // The login page itself should be accessible to everyone.
                )
                // Configure logout functionality.
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // The URL to trigger logout (typically a POST).
                        .logoutSuccessUrl("/auth/login?logout=true") // Where to redirect after a successful logout.
                        .permitAll()
                )
                .build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring Bean.
     * <p>
     * The AuthenticationManager is the core of Spring Security's authentication mechanism.
     * This bean is necessary for the framework to handle the authentication process defined
     * by the {@code DaoAuthenticationProvider} (which is configured automatically).
     *
     * @param config the {@link AuthenticationConfiguration} provided by Spring Boot.
     * @return the configured {@link AuthenticationManager}.
     * @throws Exception if the manager cannot be retrieved.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the password encoding strategy for the application.
     * <p>
     * This bean declares that {@link BCryptPasswordEncoder} should be used for all password
     * hashing and verification. BCrypt is a strong, adaptive hashing function and is the
     * recommended standard for password storage. Spring Security will automatically use this
     * bean to encode passwords during registration and compare them during login.
     *
     * @return an instance of {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

