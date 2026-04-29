package com.readit.backend.service;

import com.readit.backend.dto.UserDTO;
import com.readit.backend.dto.UserProfileUpdateRequest;
import com.readit.backend.entity.User;
import com.readit.backend.exception.DuplicateResourceException;
import com.readit.backend.exception.InvalidPasswordException;
import com.readit.backend.exception.ResourceNotFoundException;
import com.readit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getName() != null) user.setFullName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());

        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    @Transactional
    public UserDTO updateCurrentUser(String username, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        String resolvedName = request.getFullName() != null && !request.getFullName().isBlank()
                ? request.getFullName().trim()
                : request.getName();
        if (resolvedName != null && !resolvedName.isBlank()) {
            user.setFullName(resolvedName.trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail().trim());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new InvalidPasswordException("Current password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return toDTO(userRepository.save(user));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        dto.setRole(user.getRole().name());
        dto.setName(user.getFullName());
        return dto;
    }
}
