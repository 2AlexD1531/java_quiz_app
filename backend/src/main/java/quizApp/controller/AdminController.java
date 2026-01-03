package quizApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quizApp.service.AdminService;
import quizApp.model.dto.UserDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin")

public class AdminController {
    @Autowired
    public AdminService adminService;

    @GetMapping("/all-users")
    public ResponseEntity<List<UserDTO>> getUsers() {
        List<UserDTO> allUsers = adminService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {

        UserDTO user = adminService.getUserById(id);
        return ResponseEntity.ok(user);

    }

    @PutMapping("/user/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {

        String newRole = request.get("role").toUpperCase();
        UserDTO updatedUser = adminService.updateUserRole(id, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        adminService.deleteUser(id);
        return ResponseEntity.ok().body(Map.of("message", "Пользователь успешно удален"));

    }
}