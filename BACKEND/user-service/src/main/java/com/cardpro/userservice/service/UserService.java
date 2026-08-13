package com.cardpro.userservice.service;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
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
}