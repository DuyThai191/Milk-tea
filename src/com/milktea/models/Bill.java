package com.milktea.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String cashierUsername;
    private String cashierName;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();
    private double discountPercent; // 0 - 100%
    private double customerCash;    // Tiền khách đưa
    private String customerPhone;

    public Bill() {}

    public Bill(String id, String cashierUsername, String cashierName) {
        this.id = id;
        this.cashierUsername = cashierUsername;
        this.cashierName = cashierName;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.discountPercent = 0.0;
        this.customerCash = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCashierUsername() {
        return cashierUsername;
    }

    public void setCashierUsername(String cashierUsername) {
        this.cashierUsername = cashierUsername;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (items == null) items = new ArrayList<>();
        items.add(item);
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = Math.max(0, Math.min(100, discountPercent));
    }

    public double getCustomerCash() {
        return customerCash;
    }

    public void setCustomerCash(double customerCash) {
        this.customerCash = customerCash;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    /**
     * Tổng tiền trước giảm giá
     */
    public double getSubtotal() {
        if (items == null) return 0.0;
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    /**
     * Số tiền được giảm giá
     */
    public double getDiscountAmount() {
        return getSubtotal() * (discountPercent / 100.0);
    }

    /**
     * Tổng tiền thực thanh toán sau giảm giá
     */
    public double getFinalTotal() {
        return Math.max(0.0, getSubtotal() - getDiscountAmount());
    }

    /**
     * Tiền thừa trả khách
     */
    public double getChangeAmount() {
        return Math.max(0.0, customerCash - getFinalTotal());
    }

    /**
     * Tổng số lượng ly trong hóa đơn
     */
    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
