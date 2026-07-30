package com.cardpro.userservice.service;

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
}