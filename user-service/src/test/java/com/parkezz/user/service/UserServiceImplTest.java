package com.parkezz.user.service;

import com.parkezz.user.dto.RegisterRequest;
import com.parkezz.user.dto.UserInternalResponse;
import com.parkezz.user.dto.UserResponse;
import com.parkezz.user.entity.Role;
import com.parkezz.user.entity.User;
import com.parkezz.user.exception.ResourceNotFoundException;
import com.parkezz.user.exception.UserAlreadyExistsException;
import com.parkezz.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John Doe", "john@example.com", "password123", "9876543210", Role.CUSTOMER);
    }

    @Test
    void register_successfulRegistration_returnsUserResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(1L);
            return u;
        });

        UserResponse response = userService.register(registerRequest);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_passwordIsNeverStoredInPlainText() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(registerRequest);

        verify(userRepository).save(argThat(user -> user.getPassword().equals("$2a$10$hashedvalue")
                && !user.getPassword().equals("password123")));
    }

    @Test
    void getInternalByEmail_userExists_returnsInternalResponseWithPassword() {
        User user = User.builder()
                .userId(1L).name("John").email("john@example.com")
                .password("hashed").phone("9876543210").role(Role.CUSTOMER)
                .build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserInternalResponse response = userService.getInternalByEmail("john@example.com");

        assertThat(response.getPassword()).isEqualTo("hashed");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getInternalByEmail_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getInternalByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_ownProfile_returnsUserResponse() {
        User user = User.builder()
                .userId(1L).name("John").email("john@example.com")
                .password("hashed").phone("9876543210").role(Role.CUSTOMER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L, 1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getById_notOwnProfile_throwsUnauthorizedActionException() {
        assertThatThrownBy(() -> userService.getById(1L, 2L))
                .isInstanceOf(com.parkezz.user.exception.UnauthorizedActionException.class)
                .hasMessageContaining("own profile");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void update_ownProfile_updatesAndReturnsUserResponse() {
        User existing = User.builder()
                .userId(1L).name("John").email("john@example.com")
                .password("hashed").phone("9876543210").role(Role.CUSTOMER)
                .build();
        com.parkezz.user.dto.UserUpdateRequest updateRequest =
                new com.parkezz.user.dto.UserUpdateRequest("John Updated", "john.new@example.com", "9998887776");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("john.new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(1L, updateRequest, 1L);

        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getEmail()).isEqualTo("john.new@example.com");
    }

    @Test
    void update_emailBelongsToAnotherUser_throwsUserAlreadyExistsException() {
        User existing = User.builder()
                .userId(1L).name("John").email("john@example.com")
                .password("hashed").phone("9876543210").role(Role.CUSTOMER)
                .build();
        User other = User.builder().userId(2L).email("taken@example.com").build();
        com.parkezz.user.dto.UserUpdateRequest updateRequest =
                new com.parkezz.user.dto.UserUpdateRequest("John", "taken@example.com", "9876543210");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(1L, updateRequest, 1L))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        com.parkezz.user.dto.UserUpdateRequest updateRequest =
                new com.parkezz.user.dto.UserUpdateRequest("John", "john@example.com", "9876543210");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, updateRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_notOwnProfile_throwsUnauthorizedActionException() {
        com.parkezz.user.dto.UserUpdateRequest updateRequest =
                new com.parkezz.user.dto.UserUpdateRequest("John", "john@example.com", "9876543210");

        assertThatThrownBy(() -> userService.update(1L, updateRequest, 2L))
                .isInstanceOf(com.parkezz.user.exception.UnauthorizedActionException.class);

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void delete_ownProfile_deletesSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L, 1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_notOwnProfile_throwsUnauthorizedActionException() {
        assertThatThrownBy(() -> userService.delete(1L, 2L))
                .isInstanceOf(com.parkezz.user.exception.UnauthorizedActionException.class);

        verify(userRepository, never()).existsById(anyLong());
    }
}
