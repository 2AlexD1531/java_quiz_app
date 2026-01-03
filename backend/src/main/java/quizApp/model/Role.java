package quizApp.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;


public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.name()));

        switch (this) {
            case ROLE_ADMIN:
                authorities.add(new SimpleGrantedAuthority("READ"));
                authorities.add(new SimpleGrantedAuthority("WRITE"));
                authorities.add(new SimpleGrantedAuthority("DELETE"));
                authorities.add(new SimpleGrantedAuthority("USER_MANAGEMENT"));
                break;
            case ROLE_USER:
                authorities.add(new SimpleGrantedAuthority("READ"));
                authorities.add(new SimpleGrantedAuthority("WRITE"));
                break;
        }

        return authorities;
    }
}

