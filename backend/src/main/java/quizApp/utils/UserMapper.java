package quizApp.utils;

import org.springframework.stereotype.Component;
import quizApp.model.User;
import quizApp.model.dto.UserDTO;
import quizApp.model.dto.UserPrincipal;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }


    public UserDTO toDTO(UserPrincipal userPrincipal) {
        return new UserDTO(
                userPrincipal.getId(),
                userPrincipal.getRealUsername(),
                userPrincipal.getUsername(),
                userPrincipal.getRole()
        );
    }

    public User toEntity(String username, String email, String encodedPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        return user;
    }
}