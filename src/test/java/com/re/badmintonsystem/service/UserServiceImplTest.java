package com.re.badmintonsystem.service;

import com.re.badmintonsystem.entity.Role;
import com.re.badmintonsystem.entity.User;
import com.re.badmintonsystem.entity.enums.UserStatus;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.RoleRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = new Role("CUSTOMER", "Customer role");
        customerRole.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhone("0123456789");
        testUser.setPasswordHash("encoded_pass");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(new java.util.HashSet<>(Set.of(customerRole)));
    }

    @Test
    @DisplayName("findById - should return user when found")
    void findById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var response = userService.findById(1L);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - should throw when user not found")
    void findById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    @DisplayName("updateStatus - should change user status")
    void updateStatus_shouldChangeUserStatus() {
        var request = new com.re.badmintonsystem.dto.request.UserStatusRequest();
        request.setStatus(UserStatus.LOCKED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        userService.updateStatus(1L, request);

        assertEquals(UserStatus.LOCKED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("softDelete - should set user status to DELETED")
    void softDelete_shouldSetStatusToDeleted() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        userService.softDelete(1L);

        assertEquals(UserStatus.DELETED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("softDelete - should throw when already DELETED")
    void softDelete_whenAlreadyDeleted_shouldThrowException() {
        testUser.setStatus(UserStatus.DELETED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.softDelete(1L));
    }

    @Test
    @DisplayName("getProfile - should return user profile")
    void getProfile_shouldReturnUserProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        var response = userService.getProfile(1L);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getFullName());
    }

    @Test
    @DisplayName("findAll - should return paginated results")
    void findAll_shouldReturnPaginatedResults() {
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        var response = userService.findAll(null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }
}
