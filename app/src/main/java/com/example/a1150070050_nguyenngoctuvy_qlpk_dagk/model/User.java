package com.example.a1150070050_nguyenngoctuvy_qlpk_dagk.model;

public class User {
    private int id;
    private String username;
    private String passwordHash; // BE đang dùng trường này
    private String email;
    private String role;         // "admin" | "user"

    public User() {}
    public User(int id, String username, String passwordHash, String email, String role) {
        this.id = id; this.username = username; this.passwordHash = passwordHash;
        this.email = email; this.role = role;
    }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}
