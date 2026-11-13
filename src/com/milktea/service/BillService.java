package com.milktea.service;

import com.milktea.models.Bill;
import com.milktea.storage.BillRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BillService {
    private final BillRepository billRepo;

    public BillService(BillRepository billRepo) {
        this.billRepo = billRepo;
    }

    public List<Bill> getAllBills() {
        return billRepo.findAll();
    }

    public Optional<Bill> getBillById(String id) {
        return billRepo.findById(id);
    }

    public boolean createBill(Bill bill) {
        if (bill == null || bill.getItems().isEmpty()) {
            return false;
        }
        return billRepo.add(bill);
    }

    public List<Bill> getBillsByCashier(String username) {
        return billRepo.findAll().stream()
                .filter(b -> b.getCashierUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public double calculateTotalRevenue() {
        return billRepo.findAll().stream()
                .mapToDouble(Bill::getFinalTotal)
                .sum();
    }

    public double calculateTodayRevenue() {
        LocalDate today = LocalDate.now();
        return billRepo.findAll().stream()
                .filter(b -> b.getCreatedAt().toLocalDate().equals(today))
                .mapToDouble(Bill::getFinalTotal)
                .sum();
    }
}
