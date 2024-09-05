package com.ecart.authserver.Service;

import com.ecart.authserver.Model.EmailVerificationToken;
import com.ecart.authserver.Model.User;
import com.ecart.authserver.Repository.EmailVerificationTokenRepository;
import com.ecart.authserver.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService implements IEmailVerificationService
{

    @Value("${server.port}")
    private int SERVER_PORT;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserDetailsService userDetailsService;

    public void createEmailVerificationToken(String email)
    {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setEmail(email);
        token.setExpirationDate(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours
        token.setCreatedAt(LocalDateTime.now()); // Token valid for 24 hours
        token.setCreatedBy(email); // Token valid for 24 hours
        tokenRepository.save(token);

        sendVerificationEmail(email, token.getToken());
    }

    private void sendVerificationEmail(String email, String token)
    {
        String verifyUrl = "http://localhost:" + SERVER_PORT +"/auth/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification Request");
        message.setText("To verify your email, click the link below:\n" + verifyUrl);
        mailSender.send(message);
    }

    public boolean validateEmailVerificationToken(String token)
    {
        EmailVerificationToken verifyToken = tokenRepository.findByToken(token);
        return verifyToken != null && verifyToken.getExpirationDate().isAfter(LocalDateTime.now());
    }

    public void verifyEmail(String token)
    {
        EmailVerificationToken verifyToken = tokenRepository.findByToken(token);
        if (verifyToken != null && validateEmailVerificationToken(token)) {
            User user = userRepository.findByEmail(verifyToken.getEmail()).get();
            if (user != null) {
                user.setEmailVerified(true); // Add a field `emailVerified` to User entity
                userRepository.save(user);
            }
        }
    }
}
