package quizApp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import quizApp.exception.ResourceNotFoundException;
import quizApp.model.Role;
import quizApp.model.User;
import quizApp.model.dto.UserDTO;
import quizApp.repository.UserRepository;
import quizApp.utils.UserMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private UserDTO userDTO;
    private List<User> userList;
    private List<UserDTO> userDTOList;

    @BeforeEach
    void setUp() {
        // Setup user entity
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);

        // Setup user DTO
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setRole(Role.ROLE_USER);

        // Setup lists
        userList = Arrays.asList(user);
        userDTOList = Arrays.asList(userDTO);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // When
        List<UserDTO> result = adminService.getAllUsers();

        // Then
        assertThat(result).isEqualTo(userDTOList);
        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
        verify(userMapper).toDTO(user);
    }

    @Test
    void getAllUsers_whenNoUsersFound_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> adminService.getAllUsers())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("List users is empty");

        verify(userRepository).findAll();
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // When
        UserDTO result = adminService.getUserById(1L);

        // Then
        assertThat(result).isEqualTo(userDTO);
        verify(userRepository).findById(1L);
        verify(userMapper).toDTO(user);
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void updateUserRole_shouldUpdateAndReturnUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setRole(Role.ROLE_ADMIN);

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setUsername("testuser");
        updatedUserDTO.setEmail("test@example.com");
        updatedUserDTO.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedUserDTO);

        // When
        UserDTO result = adminService.updateUserRole(1L, "ROLE_ADMIN");

        // Then
        assertThat(result).isEqualTo(updatedUserDTO);
        assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDTO(updatedUser);

        // Verify that user role was updated
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updateUserRole_withLowerCaseRole_shouldConvertToUpperCase() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRole(Role.ROLE_ADMIN);

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedUserDTO);

        // When - передаем роль в нижнем регистре
        UserDTO result = adminService.updateUserRole(1L, "role_admin");

        // Then - роль должна быть конвертирована в верхний регистр
        assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository).save(user);
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updateUserRole_withSpacesInRole_shouldTrimAndConvert() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRole(Role.ROLE_ADMIN);

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedUserDTO);

        // When - передаем роль с пробелами
        UserDTO result = adminService.updateUserRole(1L, "  role_admin  ");

        // Then - пробелы должны быть удалены
        assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository).save(user);
        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void updateUserRole_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.updateUserRole(999L, "ROLE_ADMIN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void updateUserRole_whenInvalidRole_shouldThrowIllegalArgumentException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> adminService.updateUserRole(1L, "INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid role: INVALID_ROLE. Valid roles are: ROLE_USER, ROLE_ADMIN");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void updateUserRole_withNullRole_shouldThrowIllegalArgumentException() {
        // Given
        // Не нужно мокать userRepository.findById, так как проверка на null происходит до обращения к БД

        // When & Then
        assertThatThrownBy(() -> adminService.updateUserRole(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null or empty");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void updateUserRole_withEmptyRole_shouldThrowIllegalArgumentException() {
        // Given
        // Не нужно мокать userRepository.findById, так как проверка на empty происходит до обращения к БД

        // When & Then
        assertThatThrownBy(() -> adminService.updateUserRole(1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null or empty");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void updateUserRole_withBlankRole_shouldThrowIllegalArgumentException() {
        // Given
        // Не нужно мокать userRepository.findById, так как проверка на blank происходит до обращения к БД

        // When & Then
        assertThatThrownBy(() -> adminService.updateUserRole(1L, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null or empty");

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        adminService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_whenRepositoryThrowsException_shouldPropagateException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Database error")).when(userRepository).delete(user);

        // When & Then
        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void getAllUsers_whenRepositoryThrowsException_shouldPropagateException() {
        // Given
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> adminService.getAllUsers())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(userRepository).findAll();
        verify(userMapper, never()).toDTO(any(User.class));
    }
}