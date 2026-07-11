package com.group1.recruitment.dto;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;

public class UserForm {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Long roleId;
    private AccountStatus status;
    private String password;
    private String confirmPassword;

    public UserForm() {
    }

    public static UserForm from(User user) {
        UserForm f = new UserForm();
        f.setId(user.getId());
        f.setFullName(user.getFullName());
        f.setUsername(user.getUsername());
        f.setEmail(user.getEmail());
        f.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        f.setStatus(user.getStatus());
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
