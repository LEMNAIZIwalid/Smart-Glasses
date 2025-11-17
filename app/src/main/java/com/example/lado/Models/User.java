package com.example.lado.Models;

public class User {
    private String username;
    private String email;
    private String phone;
    private String password;  // 🔹 ajouté

    // Constructeur vide requis par Firebase
    public User() {}

    // Constructeur avec mot de passe
    public User(String username, String email, String password, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // Getters et setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
