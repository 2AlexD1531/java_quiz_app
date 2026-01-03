package quizApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quizApp.exception.ResourceNotFoundException;
import quizApp.model.Role;
import quizApp.model.User;
import quizApp.model.dto.UserDTO;
import quizApp.repository.UserRepository;
import quizApp.utils.UserMapper;

import java.util.List;

@Service
@Transactional
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("List users is empty");
        }
        return users.stream().map(userMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    public UserDTO updateUserRole(Long id, String newRole) {

        if (newRole == null || newRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }


        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        try {
            Role role = Role.valueOf(newRole.trim().toUpperCase());

            user.setRole(role);
            User updatedUser = userRepository.save(user);
            return userMapper.toDTO(updatedUser);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole + ". Valid roles are: ROLE_USER, ROLE_ADMIN");
        }
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}