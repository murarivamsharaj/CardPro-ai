package com.cardpro.auth.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global platform configuration for public self-registration.
 *
 * <p>Kept intentionally in-memory (single-instance auth-service) so no schema
 * changes are required. The flag defaults to enabled and can be flipped by an
 * admin through {@code PUT /api/v1/auth/admin/config/registration}. When
 * disabled, {@code POST /register} rejects new accounts.
 */
@Service
public class RegistrationConfigService {

    private final AtomicBoolean registrationEnabled = new AtomicBoolean(true);

    public boolean isRegistrationEnabled() {
        return registrationEnabled.get();
    }

    public void setRegistrationEnabled(boolean enabled) {
        registrationEnabled.set(enabled);
    }
}
