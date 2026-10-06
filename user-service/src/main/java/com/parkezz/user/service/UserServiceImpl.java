package com.parkezz.user.service;

import com.parkezz.user.dto.RegisterRequest;
import com.parkezz.user.dto.UserInternalResponse;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.dto.UserUpdateRequest;
import com.parkezz.user.entity.User;
import com.parkezz.user.exception.ResourceNotFoundException;
import com.parkezz.user.exception.UserAlreadyExistsException;
import com.parkezz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Registration request received for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed because email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with id={}", saved.getUserId());

        return toResponse(saved);
    }

    @Override
    public UserInternalResponse getInternalByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserInternalResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getById(Long id, Long requesterId) {
        if (!id.equals(requesterId)) {
            throw new com.parkezz.user.exception.UnauthorizedActionException("You can only view your own profile");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request, Long requesterId) {
        if (!id.equals(requesterId)) {
            throw new com.parkezz.user.exception.UnauthorizedActionException("You can only update your own profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getUserId().equals(id)) {
                throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
            }
        });

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id, Long requesterId) {
        if (!id.equals(requesterId)) {
            throw new com.parkezz.user.exception.UnauthorizedActionException("You can only delete your own profile");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with id={}", id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
