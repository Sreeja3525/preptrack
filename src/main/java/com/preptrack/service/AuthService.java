package com.preptrack.service;

import com.preptrack.domain.User;
import com.preptrack.dto.request.LoginRequest;
import com.preptrack.dto.request.RegisterRequest;
import com.preptrack.dto.response.AuthResponse;
import com.preptrack.exception.DuplicateResourceException;
import com.preptrack.repository.UserRepository;
import com.preptrack.security.JwtTokenProvider;
import com.preptrack.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))  // BCrypt hash
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // authenticate() throws BadCredentialsException if password doesn't match
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail());
    }
}
