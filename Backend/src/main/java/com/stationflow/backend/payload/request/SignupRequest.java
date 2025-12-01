package com.stationflow.backend.payload.request;

import java.util.Set;

import jakarta.validation.constraints.*;

public class SignupRequest {
    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3 ile 20 karakter arasında olmalıdır")
    private String username;
 
    @NotBlank(message = "E-posta boş olamaz")
    @Size(max = 50, message = "E-posta en fazla 50 karakter olabilir")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;
    
    private Set<String> role;
    
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 40, message = "Şifre 6 ile 40 karakter arasında olmalıdır")
    private String password;
    
    private String recaptchaToken;

    public SignupRequest() {
    }

    public SignupRequest(String username, String email, Set<String> role, String password) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
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

    public Set<String> getRole() {
        return role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRecaptchaToken() {
        return recaptchaToken;
    }

    public void setRecaptchaToken(String recaptchaToken) {
        this.recaptchaToken = recaptchaToken;
    }
}
