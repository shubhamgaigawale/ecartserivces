package com.ecart.authserver.Service;

import com.ecart.authserver.Model.PasswordResetToken;
import com.ecart.authserver.Model.User;
import com.ecart.authserver.Repository.PasswordResetTokenRepository;
import com.ecart.authserver.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService implements IPasswordResetService{

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createPasswordResetToken(String email)
    {
        User user = userService.findByEmail(email).get();

        if (user != null)
        {
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setEmail(email);
            token.setExpirationDate(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
            token.setCreatedAt(LocalDateTime.now()); // Token valid for 24 hours
            token.setCreatedBy(email); // Token valid for 24 hours
            tokenRepository.save(token);

            sendResetEmail(email, token.getToken());
        }
    }

    private void sendResetEmail(String email, String token) {
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetUrl);
        mailSender.send(message);
    }

    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        return resetToken != null && resetToken.getExpirationDate().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken != null && validatePasswordResetToken(token)) {
            // Update the user's password
            User user = userRepository.findByEmail(resetToken.getEmail()).get();
            if (user != null) {
                user.setPassword(passwordEncoder.encode(newPassword)); // You should encode this password before saving
                userRepository.save(user);
            }
        }
    }
}
