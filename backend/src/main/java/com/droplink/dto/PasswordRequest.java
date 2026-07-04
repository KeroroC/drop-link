package com.droplink.dto;

public class PasswordRequest {

    private String password;

    public PasswordRequest() {}

    public PasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
