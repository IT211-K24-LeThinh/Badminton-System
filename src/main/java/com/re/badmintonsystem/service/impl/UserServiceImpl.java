package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.*;
import com.re.badmintonsystem.dto.response.PagedResponse;
import com.re.badmintonsystem.dto.response.UserResponse;
import com.re.badmintonsystem.entity.Role;
import com.re.badmintonsystem.entity.User;
import com.re.badmintonsystem.entity.enums.UserStatus;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.mapper.UserMapper;
import com.re.badmintonsystem.repository.RoleRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.service.FileUploadService;
import com.re.badmintonsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           FileUploadService fileUploadService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> findAll(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (search != null && !search.isBlank()) {
            if (status != null && !status.isBlank()) {
                userPage = userRepository.findByStatusAndSearch(
                        UserStatus.valueOf(status.toUpperCase()), search, pageable);
            } else {
                userPage = userRepository.findBySearch(search, pageable);
            }
        } else if (status != null && !status.isBlank()) {
            userPage = userRepository.findByStatus(
                    UserStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> content = userPage.getContent().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages(), userPage.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);

        // Assign roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.isEmpty()) {
                throw new BadRequestException("No valid roles found");
            }
            user.setRoles(roles);
        } else {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));
            user.setRoles(new HashSet<>(Set.of(customerRole)));
        }

        user = userRepository.save(user);
        log.info("Admin created user: {}", user.getUsername());

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        log.info("Updated user: {}", user.getUsername());

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(request.getStatus());
        userRepository.save(user);

        log.info("Updated status of user {} to {}", user.getUsername(), request.getStatus());
    }

    @Override
    @Transactional
    public UserResponse updateRoles(Long id, UserRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
        if (roles.isEmpty()) {
            throw new BadRequestException("No valid roles found");
        }

        user.setRoles(roles);
        user = userRepository.save(user);

        log.info("Updated roles for user: {}", user.getUsername());

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new BadRequestException("User is already deleted");
        }

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("Soft deleted user: {}", user.getUsername());
    }

    // ========== Profile ==========

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getUsername());

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var uploadResult = fileUploadService.upload(file);
        user.setAvatarUrl(uploadResult.getUrl());
        userRepository.save(user);

        log.info("Avatar uploaded for user {}: {}", user.getUsername(), uploadResult.getUrl());
        return uploadResult.getUrl();
    }
}
