package com.milktea.models;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role {
        ADMIN("Quản trị viên"),
        STAFF("Nhân viên thu ngân");

        private final String title;

        Role(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    private String id;
    private String username;
    private String fullName;
    private String password;
    private String phone;
    private Role role;
    private boolean active = true;

    public User() {}

    public User(String id, String username, String fullName, String password, String phone, Role role) {
        this.id = id;
        this.username = username.toLowerCase().trim();
        this.fullName = fullName;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", fullName, username, role.getTitle());
    }
}
