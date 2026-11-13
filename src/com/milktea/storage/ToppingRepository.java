package com.milktea.storage;

import com.milktea.models.Topping;
import java.util.*;

public class ToppingRepository {
    private static final String FILE_PATH = "data/toppings.dat";
    private final List<Topping> toppings;

    public ToppingRepository() {
        List<Topping> loaded = StorageUtils.loadObject(FILE_PATH, new ArrayList<>());
        this.toppings = loaded != null ? loaded : new ArrayList<>();

        if (this.toppings.isEmpty()) {
            initDefaultToppings();
        }
    }

    private void initDefaultToppings() {
        toppings.add(new Topping(UUID.randomUUID().toString(), "Trân Châu Đen Hoàng Gia", 5000));
        toppings.add(new Topping(UUID.randomUUID().toString(), "Trân Châu Trắng Ngọc Trai", 7000));
        toppings.add(new Topping(UUID.randomUUID().toString(), "Pudding Trứng Mềm Mịn", 6000));
        toppings.add(new Topping(UUID.randomUUID().toString(), "Kem Cheese Macchiato", 10000));
        toppings.add(new Topping(UUID.randomUUID().toString(), "Thạch Củ Năng Giòn", 8000));
        toppings.add(new Topping(UUID.randomUUID().toString(), "Sương Sáo Thảo Mộc", 5000));
        save();
    }

    public synchronized boolean save() {
        return StorageUtils.saveObject(FILE_PATH, toppings);
    }

    public synchronized List<Topping> findAll() {
        return new ArrayList<>(toppings);
    }

    public synchronized Optional<Topping> findById(String id) {
        if (id == null) return Optional.empty();
        return toppings.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public synchronized boolean add(Topping topping) {
        if (topping == null) return false;
        toppings.add(topping);
        return save();
    }

    public synchronized boolean update(Topping topping) {
        if (topping == null) return false;
        for (int i = 0; i < toppings.size(); i++) {
            if (toppings.get(i).getId().equals(topping.getId())) {
                toppings.set(i, topping);
                return save();
            }
        }
        return false;
    }

    public synchronized boolean delete(String id) {
        if (id == null) return false;
        boolean removed = toppings.removeIf(t -> t.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }
}
