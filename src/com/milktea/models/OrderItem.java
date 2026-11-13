package com.milktea.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Product product;
    private Size size;
    private List<Topping> toppings = new ArrayList<>();
    private int sugarPercent; // 0, 30, 50, 70, 100
    private int icePercent;   // 0, 50, 100
    private int quantity;
    private String note;

    public OrderItem() {}

    public OrderItem(Product product, Size size, List<Topping> toppings, int sugarPercent, int icePercent, int quantity, String note) {
        this.product = product;
        this.size = size != null ? size : Size.S;
        if (toppings != null) {
            this.toppings = new ArrayList<>(toppings);
        }
        this.sugarPercent = sugarPercent;
        this.icePercent = icePercent;
        this.quantity = Math.max(1, quantity);
        this.note = note != null ? note : "";
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Topping> toppings) {
        this.toppings = toppings != null ? toppings : new ArrayList<>();
    }

    public int getSugarPercent() {
        return sugarPercent;
    }

    public void setSugarPercent(int sugarPercent) {
        this.sugarPercent = sugarPercent;
    }

    public int getIcePercent() {
        return icePercent;
    }

    public void setIcePercent(int icePercent) {
        this.icePercent = icePercent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Đơn giá 1 ly = Giá gốc sản phẩm + Phụ thu Size + Tổng giá Topping
     */
    public double getUnitPrice() {
        double total = product != null ? product.getBasePrice() : 0.0;
        if (size != null) {
            total += size.getExtraPrice();
        }
        if (toppings != null) {
            for (Topping t : toppings) {
                total += t.getPrice();
            }
        }
        return total;
    }

    /**
     * Thành tiền = Đơn giá * Số lượng
     */
    public double getTotalPrice() {
        return getUnitPrice() * quantity;
    }

    /**
     * Chuỗi mô tả chi tiết pha chế: Size, Đường, Đá, Topping
     */
    public String getCustomizationSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(size != null ? size.name() : "S");
        sb.append(" | ").append(sugarPercent).append("% Đường, ").append(icePercent).append("% Đá");
        if (toppings != null && !toppings.isEmpty()) {
            sb.append(" | Topping: ");
            for (int i = 0; i < toppings.size(); i++) {
                sb.append(toppings.get(i).getName());
                if (i < toppings.size() - 1) sb.append(", ");
            }
        }
        if (note != null && !note.trim().isEmpty()) {
            sb.append(" (").append(note.trim()).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s (%s) x%d = %,.0f đ",
                product != null ? product.getName() : "Món",
                getCustomizationSummary(),
                quantity,
                getTotalPrice());
    }
}
