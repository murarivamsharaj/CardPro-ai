package com.cardpro.auth.security;

import com.cardpro.auth.service.JwtService;
import com.cardpro.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * {@link AuthenticationProvider} for JWT-based authentication.
 *
 * <p>Validates a JWT token presented as the credentials in an
 * {@link UsernamePasswordAuthenticationToken}, checks the token blacklist,
 * loads the {@link UserPrincipal} from the database, and returns an
 * authenticated token with granted authorities.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final CardProUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();

        // 1. Validate JWT structure and signature
        if (!jwtService.isTokenValid(token)) {
            throw new BadCredentialsException("Invalid or expired JWT token");
        }

        // 2. Check token blacklist (logout)
        String tokenId = jwtService.extractTokenId(token);
        if (tokenBlacklistService.isTokenBlacklisted(tokenId)) {
            throw new BadCredentialsException("JWT token has been revoked");
        }

        // 3. Extract user and load principal
        String userId = jwtService.extractUserId(token);
        UserPrincipal principal = userDetailsService.loadUserById(java.util.UUID.fromString(userId));

        // 4. Return authenticated token
        return UsernamePasswordAuthenticationToken.authenticated(
            principal,
            token,
            principal.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
