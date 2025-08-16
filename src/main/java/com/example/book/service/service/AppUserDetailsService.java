package com.example.book.service.service;

import com.example.book.service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * A custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * This service is a core component of the Spring Security authentication process.
 * Its sole responsibility is to locate a user based on their username (in this case,
 * their email address) and return a {@link UserDetails} object.
 * <p>
 * Spring Security's {@code DaoAuthenticationProvider} will then use the returned
 * {@code UserDetails} object to perform password comparison and check the account's
 * status (e.g., if it's enabled, locked, etc.).
 *
 * @see UserDetailsService
 * @see org.springframework.security.authentication.dao.DaoAuthenticationProvider
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the provided email address.
     * <p>
     * This method is called by the Spring Security framework during the authentication
     * process when a user attempts to log in.
     *
     * @param email the email address identifying the user whose data is required.
     * @return a {@link UserDetails} object containing the user's information. In this
     *         application, the {@code com.example.book.service.model.User} entity
     *         itself implements {@code UserDetails}.
     * @throws UsernameNotFoundException if the user could not be found with the given email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email '" + email + "' not found"));
    }
}