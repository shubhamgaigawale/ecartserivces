package com.ecart.authserver.Security;

import com.ecart.authserver.Model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // The remaining methods can return default values or based on the User entity
    @Override
    public boolean isAccountNonExpired() {
        return true; // Modify as per your requirements
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Modify as per your requirements
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Modify as per your requirements
    }
}
