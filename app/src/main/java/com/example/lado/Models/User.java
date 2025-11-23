package com.example.lado.Models;

import java.util.Map;

public class User {

    private String username;
    private String email;
    private String phone;
    private String profile_image;

    // ⚡ sensors et notifications sont des Maps pour Firebase
    private Map<String, Object> sensors;
    private Map<String, Object> notifications;

    public User() { }

    public User(String username, String email, String phone, String profile_image) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.profile_image = profile_image;
    }

    // ----- Getters -----
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getProfile_image() { return profile_image; }
    public Map<String, Object> getSensors() { return sensors; }
    public Map<String, Object> getNotifications() { return notifications; }

    // ----- Setters -----
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfile_image(String profile_image) { this.profile_image = profile_image; }
    public void setSensors(Map<String, Object> sensors) { this.sensors = sensors; }
    public void setNotifications(Map<String, Object> notifications) { this.notifications = notifications; }
}
