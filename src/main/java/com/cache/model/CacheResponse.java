package com.cache.model;

public class CacheResponse {
    private String status;
    private String message;
    private String key;
    private String value;

    // Constructors
    public CacheResponse() {
    }

    public static CacheResponse success(String message) {
        CacheResponse response = new CacheResponse();
        response.setStatus("OK");
        response.setMessage(message);
        return response;
    }

    public static CacheResponse success(String key, String value) {
        CacheResponse response = new CacheResponse();
        response.setStatus("OK");
        response.setKey(key);
        response.setValue(value);
        return response;
    }

    public static CacheResponse success(String message, String key, String value) {
        CacheResponse response = new CacheResponse();
        response.setStatus("OK");
        response.setMessage(message);
        response.setKey(key);
        response.setValue(value);
        return response;
    }

    public static CacheResponse error(String message) {
        CacheResponse response = new CacheResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}