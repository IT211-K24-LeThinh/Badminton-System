package com.re.badmintonsystem.security;

import com.re.badmintonsystem.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean locked;
    private final Set<String> roleNames;

    public CustomUserDetails(Long id, String username, String email, String password,
                             Set<Role> roles, boolean enabled, boolean locked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.locked = locked;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        this.roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
