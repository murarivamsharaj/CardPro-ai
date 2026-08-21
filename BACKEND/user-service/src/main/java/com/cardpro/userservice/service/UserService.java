package com.cardpro.userservice.service;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.dto.WebhookUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);


    UserResponse getProfileByEmail(String email);


    UserResponse updateProfile(String email, ProfileUpdateRequest request);


    UserResponse updateNotificationPreference(String email, NotificationPreferenceRequest request);


    UserResponse regenerateApiKey(String email);


    UserResponse updateWebhookUrl(String email, WebhookUpdateRequest request);


    UserResponse deleteAccount(String email);


    boolean getWatermarkPreference(String email);


    UserResponse upgradeToPro(String email);
}