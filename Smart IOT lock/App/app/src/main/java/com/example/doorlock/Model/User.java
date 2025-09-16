package com.example.doorlock.Model;

public class User {
    private String id, name, email, role, nic;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String id, String name, String email, String role, String nic) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.nic = nic;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getNic() { return nic; }
}
