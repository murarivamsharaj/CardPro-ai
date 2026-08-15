package com.cardpro.userservice.service;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.dto.WebhookUpdateRequest;
import java.util.List;

public interface UserService {
    // -> ADD THIS LINE:
    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);

    /** Profile of the user identified by the JWT email claim. */
    UserResponse getProfileByEmail(String email);

    /** Upsert profile details (display name, phone, job title). */
    UserResponse updateProfile(String email, ProfileUpdateRequest request);

    /** Persist + broadcast the email-notification preference. */
    UserResponse updateNotificationPreference(String email, NotificationPreferenceRequest request);

    /** Generate a fresh API key (UUID) for the user's developer integrations. */
    UserResponse regenerateApiKey(String email);

    /** Save (or clear) the webhook URL used by future CRM integrations. */
    UserResponse updateWebhookUrl(String email, WebhookUpdateRequest request);

    /**
     * Soft-delete the caller's own account (active=false). Cross-service
     * cascade of cards/leads is out of scope for a single transaction, so the
     * row is retained and the account is simply barred from further use.
     */
    UserResponse deleteAccount(String email);

    /**
     * Internal-only: the card owner's removeWatermark Pro preference, used by
     * card-service to decide whether to render the watermark on public cards.
     */
    boolean getWatermarkPreference(String email);
}