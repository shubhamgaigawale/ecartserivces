package com.ecart.authserver.Payload;

import com.ecart.authserver.Model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest
{
    @NotEmpty(message = "Username can not be a null or empty")
    @Size(min = 5, max = 30, message = "The length of the username should be between 5 and 30")
    private String username;

    @NotEmpty(message = "Email address can not be a null or empty")
    @Email(message = "Email address should be a valid value")
    private String email;

    @NotEmpty
    @Size(min = 6, max = 40)
    private String password;
    private Set<Role> roles;
}
