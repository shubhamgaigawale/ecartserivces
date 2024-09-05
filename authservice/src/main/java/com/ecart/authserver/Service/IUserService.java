package com.ecart.authserver.Service;

import com.ecart.authserver.Model.Role;
import com.ecart.authserver.Model.User;

import java.util.Optional;
import java.util.Set;

public interface IUserService
{
    User registerUser(String username, String email, String password, Set<Role> roles);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean isEmailVerified(String username);
}
