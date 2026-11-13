package com.milktea.storage;

import java.io.*;

public class StorageUtils {

    @SuppressWarnings("unchecked")
    public static <T> T loadObject(String filePath, T defaultVal) {
        File file = new File(filePath);
        if (!file.exists()) {
            return defaultVal;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            return (T) obj;
        } catch (Exception e) {
            System.err.println("[StorageUtils] Lỗi khi đọc file " + filePath + ": " + e.getMessage());
            return defaultVal;
        }
    }

    public static <T> boolean saveObject(String filePath, T object) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(object);
            oos.flush();
            return true;
        } catch (Exception e) {
            System.err.println("[StorageUtils] Lỗi khi lưu file " + filePath + ": " + e.getMessage());
            return false;
        }
    }
}
