package com.re.badmintonsystem.security;

import com.re.badmintonsystem.entity.User;
import com.re.badmintonsystem.entity.UserStatus;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        return mapToUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return mapToUserDetails(user);
    }

    private CustomUserDetails mapToUserDetails(User user) {
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        boolean locked = user.getStatus() == UserStatus.LOCKED;

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles(),
                enabled,
                locked
        );
    }
}
