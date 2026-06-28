package com.re.badmintonsystem.service.impl;

import com.re.badmintonsystem.dto.request.LoginRequest;
import com.re.badmintonsystem.dto.request.RegisterRequest;
import com.re.badmintonsystem.dto.response.AuthResponse;
import com.re.badmintonsystem.entity.Role;
import com.re.badmintonsystem.entity.TokenBlacklist;
import com.re.badmintonsystem.entity.User;
import com.re.badmintonsystem.entity.UserStatus;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.UnauthorizedException;
import com.re.badmintonsystem.repository.RoleRepository;
import com.re.badmintonsystem.repository.TokenBlacklistRepository;
import com.re.badmintonsystem.repository.UserRepository;
import com.re.badmintonsystem.security.JwtTokenProvider;
import com.re.badmintonsystem.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           TokenBlacklistRepository tokenBlacklistRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(Set.of(customerRole)));

        userRepository.save(user);

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roleNames);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is disabled or locked");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roleNames);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Provided token is not a refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is disabled or locked");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roleNames);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("Token refreshed for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        log.info("Logging out user");

        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("Access token is required");
        }

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new BadRequestException("Invalid or expired access token");
        }

        long expirationTime = jwtTokenProvider.getExpirationFromToken(accessToken);
        Date expiresAt = new Date(expirationTime);

        TokenBlacklist blacklistEntry = new TokenBlacklist();
        blacklistEntry.setTokenHash(String.valueOf(accessToken.hashCode()));
        blacklistEntry.setExpiresAt(expiresAt);
        tokenBlacklistRepository.save(blacklistEntry);

        log.info("Token revoked successfully");
    }
}
