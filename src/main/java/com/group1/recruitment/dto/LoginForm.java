package com.group1.recruitment.dto;

import jakarta.validation.constraints.NotBlank;

/** Backing bean for SCR-01 (User Login). */
public class LoginForm {

    @NotBlank(message = "Please enter your username or email.")
    private String usernameOrEmail;

    @NotBlank(message = "Please enter your password.")
    private String password;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
