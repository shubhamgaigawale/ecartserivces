package com.ecart.authserver.Controller;

import com.ecart.authserver.Model.Role;
import com.ecart.authserver.Model.User;
import com.ecart.authserver.Payload.JwtResponse;
import com.ecart.authserver.Payload.LoginRequest;
import com.ecart.authserver.Payload.SignupRequest;
import com.ecart.authserver.Security.jwt.JwtUtils;
import com.ecart.authserver.Service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final IPasswordResetService passwordResetService;

    private final IEmailVerificationService emailVerificationService;

    private final IUserService userService;

    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, IPasswordResetService passwordResetService,
                          IEmailVerificationService emailVerificationService, IUserService userService, JwtUtils jwtUtils)
    {
        this.authenticationManager = authenticationManager;
        this.passwordResetService = passwordResetService;
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
    {
        boolean isVerifiedUser = userService.isEmailVerified(loginRequest.getUsername());

        if (!isVerifiedUser)
        {
            User user = userService.findByUsername(loginRequest.getUsername()).orElseThrow(() ->
                    new RuntimeException("User not found"));

            emailVerificationService.createEmailVerificationToken(user.getEmail());

            return ResponseEntity.ok("Account is not verified. Verification email has been sent to your email address.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), 
                        loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken((UserDetails) authentication.getPrincipal());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow(() ->
                new RuntimeException("User not found"));
        
        List<String> roles = user.getRoles().stream()
                                 .map(Role::name)
                                 .collect(Collectors.toList());
        
        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername(),
                                                user.getEmail(), roles));
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest)
    {
        try
        {
            // Register the user
            User user = userService.registerUser(signUpRequest.getUsername(), signUpRequest.getEmail(),
                    signUpRequest.getPassword(), signUpRequest.getRoles());

            // Generate and send email verification token
            emailVerificationService.createEmailVerificationToken(user.getEmail());

            return ResponseEntity.ok("Verification email has been sent to your email address.");
        } catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email)
    {
        passwordResetService.createPasswordResetToken(email);
        return ResponseEntity.ok("Password reset email sent.");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token)
    {
        if (emailVerificationService.validateEmailVerificationToken(token))
        {
            emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok("Email verified successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired token.");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword)
    {
        if (passwordResetService.validatePasswordResetToken(token))
        {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password has been reset.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired token.");
    }
}
