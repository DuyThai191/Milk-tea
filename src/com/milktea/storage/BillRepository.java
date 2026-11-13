package com.milktea.storage;

import com.milktea.models.Bill;
import java.util.*;

public class BillRepository {
    private static final String FILE_PATH = "data/bills.dat";
    private final List<Bill> bills;

    public BillRepository() {
        List<Bill> loaded = StorageUtils.loadObject(FILE_PATH, new ArrayList<>());
        this.bills = loaded != null ? loaded : new ArrayList<>();
    }

    public synchronized boolean save() {
        return StorageUtils.saveObject(FILE_PATH, bills);
    }

    public synchronized List<Bill> findAll() {
        return new ArrayList<>(bills);
    }

    public synchronized Optional<Bill> findById(String id) {
        if (id == null) return Optional.empty();
        return bills.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public synchronized boolean add(Bill bill) {
        if (bill == null) return false;
        bills.add(0, bill); // Chèn mới nhất lên đầu danh sách
        return save();
    }

    public synchronized boolean delete(String id) {
        if (id == null) return false;
        boolean removed = bills.removeIf(b -> b.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }
}
