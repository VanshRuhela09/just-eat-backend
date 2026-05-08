package com.justeat.backend.user.service;

import com.justeat.backend.user.dto.UpdateUserRequest;
import com.justeat.backend.user.dto.UserResponse;

public interface UserService {
    UserResponse getLoggedInUser();
    UserResponse updateLoggedInUser(UpdateUserRequest request);
}

