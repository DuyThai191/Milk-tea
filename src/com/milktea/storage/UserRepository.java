package com.milktea.storage;

import com.milktea.models.User;
import java.util.*;

public class UserRepository {
    private static final String FILE_PATH = "data/users.dat";
    private final Map<String, User> userMap;

    public UserRepository() {
        Map<String, User> loaded = StorageUtils.loadObject(FILE_PATH, new HashMap<>());
        this.userMap = loaded != null ? loaded : new HashMap<>();

        // Khởi tạo tài khoản mặc định nếu chưa có
        initDefaultUsers();
    }

    private void initDefaultUsers() {
        boolean changed = false;

        // Admin mặc định
        if (!userMap.containsKey("admin")) {
            User admin = new User(
                    UUID.randomUUID().toString(),
                    "admin",
                    "Quản Trị Viên",
                    "admin123",
                    "0988888888",
                    User.Role.ADMIN
            );
            userMap.put(admin.getUsername(), admin);
            changed = true;
        }

        // Nhân viên mẫu
        if (!userMap.containsKey("staff")) {
            User staff = new User(
                    UUID.randomUUID().toString(),
                    "staff",
                    "Nguyễn Văn Thu Ngân",
                    "staff123",
                    "0977777777",
                    User.Role.STAFF
            );
            userMap.put(staff.getUsername(), staff);
            changed = true;
        }

        if (changed) {
            save();
        }
    }

    public synchronized boolean save() {
        return StorageUtils.saveObject(FILE_PATH, userMap);
    }

    public synchronized Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(userMap.get(username.toLowerCase().trim()));
    }

    public synchronized Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return userMap.values().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    public synchronized boolean add(User user) {
        if (user == null || userMap.containsKey(user.getUsername())) {
            return false;
        }
        userMap.put(user.getUsername(), user);
        return save();
    }

    public synchronized boolean update(User user) {
        if (user == null || !userMap.containsKey(user.getUsername())) {
            return false;
        }
        userMap.put(user.getUsername(), user);
        return save();
    }

    public synchronized boolean delete(String username) {
        if (username == null || !userMap.containsKey(username.toLowerCase().trim())) {
            return false;
        }
        userMap.remove(username.toLowerCase().trim());
        return save();
    }
}
