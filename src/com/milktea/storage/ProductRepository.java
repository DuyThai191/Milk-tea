package com.milktea.storage;

import com.milktea.models.Product;
import java.util.*;

public class ProductRepository {
    private static final String FILE_PATH = "data/products.dat";
    private final List<Product> products;

    public ProductRepository() {
        List<Product> loaded = StorageUtils.loadObject(FILE_PATH, new ArrayList<>());
        this.products = loaded != null ? loaded : new ArrayList<>();

        if (this.products.isEmpty()) {
            initDefaultProducts();
        }
    }

    private void initDefaultProducts() {
        products.add(new Product(UUID.randomUUID().toString(), "Trà Sữa Trân Châu Đường Đen", "Trà sữa", 35000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Sữa Ô Long Nướng", "Trà sữa", 32000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Sữa Matcha Nhật Bản", "Trà sữa", 38000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Sữa Khoai Môn Kem Béo", "Trà sữa", 35000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Đào Cam Sả", "Trà trái cây", 32000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Dâu Tằm Macchiato", "Trà trái cây", 36000));
        products.add(new Product(UUID.randomUUID().toString(), "Trà Vải Hoa Lài", "Trà trái cây", 30000));
        products.add(new Product(UUID.randomUUID().toString(), "Cà Phê Muối Béo", "Cà phê", 28000));
        products.add(new Product(UUID.randomUUID().toString(), "Bạc Xỉu Sữa Dừa", "Cà phê", 26000));
        save();
    }

    public synchronized boolean save() {
        return StorageUtils.saveObject(FILE_PATH, products);
    }

    public synchronized List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public synchronized Optional<Product> findById(String id) {
        if (id == null) return Optional.empty();
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public synchronized boolean add(Product product) {
        if (product == null) return false;
        products.add(product);
        return save();
    }

    public synchronized boolean update(Product product) {
        if (product == null) return false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                return save();
            }
        }
        return false;
    }

    public synchronized boolean delete(String id) {
        if (id == null) return false;
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }
}
