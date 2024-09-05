package com.ecart.authserver.Service;

public interface IPasswordResetService
{
    void createPasswordResetToken(String email);
    boolean validatePasswordResetToken(String token);
    void resetPassword(String token, String newPassword);
}
