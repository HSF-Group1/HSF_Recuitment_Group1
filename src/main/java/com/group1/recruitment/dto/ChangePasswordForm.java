package com.group1.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Backing bean for SCR-05 (Password Change). */
public class ChangePasswordForm {

    @NotBlank(message = "Enter your current password.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 72, message = "Password must be at least 8 characters.")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
