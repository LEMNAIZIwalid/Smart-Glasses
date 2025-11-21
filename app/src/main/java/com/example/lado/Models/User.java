package com.example.lado.Models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String email;
    private String phone;
    private Map<String, Object> sensors; // contient ultrasonic, camera, current

    public User() {
        sensors = new HashMap<>();
        sensors.put("ultrasonic", new HashMap<String, Object>() {{ put("distance", ""); }});
        sensors.put("camera", new HashMap<String, Object>() {{ put("status", ""); }});
        sensors.put("current", new HashMap<String, Object>() {{
            put("distance", "");
            put("status", "");
            put("stamp", "");
        }});
    }

    public User(String username, String email, String phone) {
        this();
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Map<String, Object> getSensors() { return sensors; }
    public void setSensors(Map<String, Object> sensors) { this.sensors = sensors; }
}
