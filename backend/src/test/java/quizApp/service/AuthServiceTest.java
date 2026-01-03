package quizApp.service;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import quizApp.model.Role;
import quizApp.model.User;
import quizApp.model.dto.*;
import quizApp.repository.UserRepository;
import quizApp.service.AuthService;
import quizApp.utils.JwtUtils;
import quizApp.utils.UserMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User sampleUser;
    private UserDTO sampleUserDTO;

    @BeforeEach
    void setUp() {
        // Setup quiz data
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("quizuser");
        registerRequest.setEmail("quiz@example.com");
        registerRequest.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("quiz@example.com");
        loginRequest.setPassword("password");

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("quizuser");
        sampleUser.setEmail("quiz@example.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole(Role.ROLE_USER);

        sampleUserDTO = new UserDTO(1L, "quizuser", "quiz@example.com", Role.ROLE_USER);
    }

   @Test
    void register_shouldCreateUserAndReturnAuthResponse() {
        // Given

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.toEntity(anyString(), anyString(), anyString())).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(sampleUserDTO);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUserDTO()).isEqualTo(sampleUserDTO);

        verify(userRepository).existsByEmail("quiz@example.com");
        verify(userRepository).existsByUsername("quizuser");
        verify(userRepository).save(sampleUser);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_withExistingUsername_shouldThrowException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldAuthenticateAndReturnAuthResponse() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(sampleUserDTO);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUserDTO()).isEqualTo(sampleUserDTO);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findByEmail("quiz@example.com");
        verify(userMapper).toDTO(sampleUser);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtUtils, never()).generateJwtToken(any(Authentication.class));
    }

    @Test
    void login_whenUserNotFound_shouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findByEmail("quiz@example.com");
    }

    @Test
    void register_shouldEncodePassword() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMapper.toEntity(anyString(), anyString(), anyString())).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(sampleUserDTO);

        // When
        authService.register(registerRequest);

        // Then
        verify(passwordEncoder).encode("password");
        verify(userMapper).toEntity("quizuser", "quiz@example.com", "encodedPassword");
    }

    @Test
    void login_shouldUseEmailAsUsernameForAuthentication() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(sampleUserDTO);

        // When
        authService.login(loginRequest);

        // Then
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("quiz@example.com", "password")
        );
    }
}