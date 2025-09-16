package com.example.doorlock.Model;

public class Key {
    private String keyUid;
    private String keyName; // This will store door_name
    private String doorId;

    public Key() {
        // Default constructor required for Firebase
    }

    public Key(String keyUid, String keyName, String doorId) {
        this.keyUid = keyUid;
        this.keyName = keyName; // Store door_name in keyName
        this.doorId = doorId;
    }

    public Key(String key, String keyName) {
    }

    public String getKeyUid() {
        return keyUid;
    }

    public String getKeyName() {
        return keyName; // door_name will be returned here
    }

    public String getDoorId() {
        return doorId;
    }
}
