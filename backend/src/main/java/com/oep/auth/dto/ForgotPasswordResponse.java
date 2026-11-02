package com.oep.auth.dto;

public class ForgotPasswordResponse {

    private String message;
    private String resetToken;
    private long expiresInMinutes;

    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(String message, String resetToken, long expiresInMinutes) {
        this.message = message;
        this.resetToken = resetToken;
        this.expiresInMinutes = expiresInMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public long getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(long expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
}
