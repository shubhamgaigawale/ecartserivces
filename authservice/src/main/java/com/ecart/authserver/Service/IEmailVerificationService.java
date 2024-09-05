package com.ecart.authserver.Service;

public interface IEmailVerificationService
{
    void createEmailVerificationToken(String email);

    boolean validateEmailVerificationToken(String token);

    void verifyEmail(String token);
}
