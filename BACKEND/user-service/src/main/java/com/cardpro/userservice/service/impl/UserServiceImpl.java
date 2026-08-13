package com.cardpro.userservice.service.impl;

import com.cardpro.userservice.dto.NotificationPreferenceRequest;
import com.cardpro.userservice.dto.ProfileUpdateRequest;
import com.cardpro.userservice.dto.UserRequest;
import com.cardpro.userservice.dto.UserResponse;
import com.cardpro.userservice.entity.User;
import com.cardpro.userservice.exception.DuplicateEmailException;
import com.cardpro.userservice.exception.UserNotFoundException;
import com.cardpro.userservice.exception.UserProfileNotFoundException;
import com.cardpro.userservice.repository.UserRepository;
import com.cardpro.userservice.service.NotificationEventPublisher;
import com.cardpro.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface.
 * Handles the business logic for user CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.debug("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(request.getProfileImage())
                .role(request.getRole()) // <-- FIXED: Added role mapping
                .build();

        user = userRepository.save(user);
        log.info("User created successfully with id: {}", user.getId());

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfileImage(request.getProfileImage());
        user.setRole(request.getRole()); // <-- FIXED: Added role update

        user = userRepository.save(user);
        log.info("User updated successfully with id: {}", user.getId());

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
        log.info("User deleted successfully with id: {}", id);
    }

    // ──────────────────────────────────────────────
    // Profile (authenticated via JWT email claim)
    // ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserProfileNotFoundException(email));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> newProfile(email));

        // Only overwrite fields the client actually sent, so a partial update
        // never wipes previously saved values.
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateNotificationPreference(String email, NotificationPreferenceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> newProfile(email));

        user.setEmailNotificationsEnabled(Boolean.TRUE.equals(request.getEnabled()));
        user = userRepository.save(user);

        // Broadcast the preference so the lead-notification pipeline (and any
        // other consumer) can honor it without hitting this database.
        notificationEventPublisher.publishNotificationPreferenceChanged(user);

        log.info("Email notification preference for {} set to {}", user.getEmail(), user.getEmailNotificationsEnabled());
        return mapToResponse(user);
    }

    /**
     * Creates a minimal profile record keyed by the JWT email. Keeps the
     * existing schema rules intact: {@code firstName}/{@code lastName} are NOT
     * NULL, so they are derived from the display name (or the email) instead of
     * being dropped.
     */
    private User newProfile(String email) {
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String firstName = localPart.isEmpty() ? "User" : localPart;
        String lastName = "";
        return User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role("ROLE_USER")
                .active(true)
                .emailNotificationsEnabled(true)
                .build();
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user the entity to map
     * @return the response DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .jobTitle(user.getJobTitle())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .active(user.isActive())
                .emailNotificationsEnabled(user.getEmailNotificationsEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}