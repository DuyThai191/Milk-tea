package com.milktea.service;

import com.milktea.models.User;
import com.milktea.storage.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                if (!user.isActive()) {
                    return Optional.empty(); // Tài khoản bị vô hiệu hóa
                }
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (user == null || oldPassword == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword.trim());
            return userRepo.update(user);
        }
        return false;
    }

    public boolean register(String username, String fullName, String password, String phone, User.Role role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        if (userRepo.findByUsername(username).isPresent()) {
            return false; // Tên đăng nhập đã tồn tại
        }

        User newUser = new User(
                UUID.randomUUID().toString(),
                username.trim(),
                fullName != null ? fullName.trim() : username.trim(),
                password.trim(),
                phone != null ? phone.trim() : "",
                role != null ? role : User.Role.STAFF
        );

        return userRepo.add(newUser);
    }
}
