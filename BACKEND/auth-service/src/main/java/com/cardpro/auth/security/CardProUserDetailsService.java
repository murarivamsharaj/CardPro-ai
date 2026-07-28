package com.cardpro.auth.security;

import com.cardpro.auth.entity.User;
import com.cardpro.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads user details from the database by email.
 * Used by {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
 * for password-based authentication and by {@link JwtAuthenticationProvider}
 * for JWT-based authentication.
 */
@Service
@RequiredArgsConstructor
public class CardProUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email
            ));
        return new UserPrincipal(user);
    }

    /**
     * Load user by ID (used by JWT authentication where we have userId from the token).
     */
    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(java.util.UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with id: " + userId
            ));
        return new UserPrincipal(user);
    }
}
