package com.re.badmintonsystem.config;

import com.re.badmintonsystem.repository.TokenBlacklistRepository;
import com.re.badmintonsystem.security.CustomUserDetailsService;
import com.re.badmintonsystem.security.JwtAuthenticationEntryPoint;
import com.re.badmintonsystem.security.JwtAuthenticationFilter;
import com.re.badmintonsystem.security.JwtTokenProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false, jsr250Enabled = false)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService customUserDetailsService,
                          TokenBlacklistRepository tokenBlacklistRepository,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService, tokenBlacklistRepository);
    }

    /**
     * CRITICAL: Disable Spring Boot auto-registration of JwtAuthenticationFilter
     * as a servlet filter. It must ONLY run inside Spring Security's filter chain.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableAutoRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(anonymous -> anonymous
                        .authorities("ROLE_ANONYMOUS")
                        .principal("anonymousUser"))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints — NO authentication required
                        .requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/refresh-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/reset-password").permitAll()
                        // Authenticated auth endpoints
                        .requestMatchers(HttpMethod.POST, "/v1/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/change-password").authenticated()
                        // Remaining public paths
                        .requestMatchers("/v1/public/**").permitAll()
                        // Court browsing (public read)
                        .requestMatchers(HttpMethod.GET, "/v1/court-complexes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/courts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/time-slots/**").permitAll()
                        // Admin only
                        .requestMatchers("/v1/admin/**").hasAuthority("ADMIN")
                        // Manager only
                        .requestMatchers("/v1/manager/**").hasAuthority("MANAGER")
                        // Authenticated endpoints
                        .requestMatchers("/v1/profile/**").authenticated()
                        .requestMatchers("/v1/files/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
