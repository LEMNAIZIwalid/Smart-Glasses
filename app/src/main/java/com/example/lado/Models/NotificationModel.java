package com.example.lado.Models;

public class NotificationModel {
    private String message;
    private long timestamp;

    public NotificationModel() {}

    public NotificationModel(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
