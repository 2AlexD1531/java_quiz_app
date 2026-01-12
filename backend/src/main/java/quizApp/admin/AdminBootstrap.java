package quizApp.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import quizApp.model.Role;
import quizApp.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminBootstrap {

    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)   // контекст поднят, таблицы точно есть
    @Transactional
    public void grantAdminToFirstUser() {
        userRepository.findById(1L).ifPresent(u -> {
            if (u.getRole() == Role.ROLE_USER) {
                u.setRole(Role.ROLE_ADMIN);
                userRepository.save(u);
            }
        });
    }
}