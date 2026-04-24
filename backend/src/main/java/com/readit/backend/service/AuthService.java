package com.readit.backend.service;

import com.readit.backend.dto.*;
import com.readit.backend.entity.User;
import com.readit.backend.repository.UserRepository;
import com.readit.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ModelMapper modelMapper;

    /**
     * Register a new user. Hashes the password with BCrypt before saving.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        User saved = userRepository.save(user);

        // Auto-login after registration
        String token = tokenProvider.generateTokenFromUsername(saved.getUsername());
        UserDTO userDTO = modelMapper.map(saved, UserDTO.class);
        userDTO.setRole(saved.getRole().name());

        return new AuthResponse(token, userDTO);
    }

    /**
     * Authenticate with username/email + password. Returns JWT on success.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()));

            String token = tokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(request.getUsernameOrEmail())
                    .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                    .orElseThrow();

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            userDTO.setRole(user.getRole().name());

            return new AuthResponse(token, userDTO);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }
}
