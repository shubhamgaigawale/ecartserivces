package com.ecart.authserver.Service;

import com.ecart.authserver.Model.Role;
import com.ecart.authserver.Model.User;
import com.ecart.authserver.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements IUserService{

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String email, String password, Set<Role> roles)
    {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailVerified(false);
        user.setCreatedBy(username);
        user.setCreatedAt(LocalDateTime.now());
        
        if (roles == null || roles.isEmpty())
        {
            Set<Role> defaultRoles = new HashSet<>();
            defaultRoles.add(Role.ROLE_USER);
            user.setRoles(defaultRoles);
        } else
        {
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    public boolean isEmailVerified(String username)
    {
        return userRepository.existsByUsernameAndEmailVerified(username, true);
    }
}
