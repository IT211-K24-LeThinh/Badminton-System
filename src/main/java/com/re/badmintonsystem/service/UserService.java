package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.request.*;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    PagedResponse<UserResponse> findAll(String search, String status, int page, int size);

    UserResponse findById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void updateStatus(Long id, UserStatusRequest request);

    UserResponse updateRoles(Long id, UserRolesRequest request);

    void softDelete(Long id);

    // Profile
    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    String uploadAvatar(Long userId, MultipartFile file);
}
