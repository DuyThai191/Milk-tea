package com.milktea.service;

import com.milktea.models.Product;
import com.milktea.models.Topping;
import com.milktea.storage.ProductRepository;
import com.milktea.storage.ToppingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository productRepo;
    private final ToppingRepository toppingRepo;

    public ProductService(ProductRepository productRepo, ToppingRepository toppingRepo) {
        this.productRepo = productRepo;
        this.toppingRepo = toppingRepo;
    }

    // --- Products ---
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Product> getAvailableProducts() {
        return productRepo.findAll().stream()
                .filter(Product::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("Tất cả")) {
            return getAvailableProducts();
        }
        return productRepo.findAll().stream()
                .filter(p -> p.isAvailable() && p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public boolean addProduct(String name, String category, double basePrice) {
        if (name == null || name.trim().isEmpty() || basePrice < 0) return false;
        Product p = new Product(UUID.randomUUID().toString(), name.trim(), category.trim(), basePrice);
        return productRepo.add(p);
    }

    public boolean updateProduct(Product product) {
        return productRepo.update(product);
    }

    public boolean deleteProduct(String id) {
        return productRepo.delete(id);
    }

    // --- Toppings ---
    public List<Topping> getAllToppings() {
        return toppingRepo.findAll();
    }

    public List<Topping> getAvailableToppings() {
        return toppingRepo.findAll().stream()
                .filter(Topping::isAvailable)
                .collect(Collectors.toList());
    }

    public boolean addTopping(String name, double price) {
        if (name == null || name.trim().isEmpty() || price < 0) return false;
        Topping t = new Topping(UUID.randomUUID().toString(), name.trim(), price);
        return toppingRepo.add(t);
    }

    public boolean updateTopping(Topping topping) {
        return toppingRepo.update(topping);
    }

    public boolean deleteTopping(String id) {
        return toppingRepo.delete(id);
    }
}
