package com.ecart.authserver.Security.oauth2;

import com.ecart.authserver.Model.Role;
import com.ecart.authserver.Model.User;
import com.ecart.authserver.Security.jwt.JwtUtils;
import com.ecart.authserver.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    @Autowired
    public OAuth2SuccessHandler(@Lazy UserService userService, JwtUtils jwtUtils)
    {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) 
                                        throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String username = oAuth2User.getAttribute("email");
        if (username == null) {
            username = oAuth2User.getAttribute("login"); // For providers like GitHub
        }

        // Fetch or create user in your database
        String finalUsername = username;

        User user = userService.findByUsername(username).orElseGet(() -> {
            // Create new user if not found
            User newUser = new User();
            newUser.setUsername(finalUsername);
            newUser.setEmail(oAuth2User.getAttribute("email"));
            newUser.setPassword(""); // No password as it's OAuth2
            Set<Role> roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
            newUser.setRoles(roles);
            newUser.setEnabled(true);
            return userService.registerUser(newUser.getUsername(), 
                                           newUser.getEmail(), 
                                           "oauth2user", // Dummy password
                                           newUser.getRoles());
        });
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getUsername());
        
        // Return the token in response (you may want to redirect or set it in a cookie)
        response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + jwt + "\"}");
    }
}
