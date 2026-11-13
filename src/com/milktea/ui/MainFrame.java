package com.milktea.ui;

import com.milktea.models.User;
import com.milktea.service.AuthService;
import com.milktea.service.BillService;
import com.milktea.service.ProductService;
import com.milktea.storage.BillRepository;
import com.milktea.storage.ProductRepository;
import com.milktea.storage.ToppingRepository;
import com.milktea.storage.UserRepository;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    // Repositories
    private final UserRepository userRepo = new UserRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final ToppingRepository toppingRepo = new ToppingRepository();
    private final BillRepository billRepo = new BillRepository();

    // Services
    private final AuthService authService = new AuthService(userRepo);
    private final ProductService productService = new ProductService(productRepo, toppingRepo);
    private final BillService billService = new BillService(billRepo);

    // Card Layout
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public MainFrame() {
        setTitle("Boba Sky - Hệ Thống Quản Lý & Bán Trà Sữa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initViews();
        add(cards);
    }

    private void initViews() {
        // Màn hình đăng nhập
        LoginPanel loginPanel = new LoginPanel(authService, this::onLoginSuccess);
        cards.add(loginPanel, "LOGIN");

        cardLayout.show(cards, "LOGIN");
    }

    private void onLoginSuccess(User user) {
        if (user.getRole() == User.Role.ADMIN) {
            AdminPanel adminPanel = new AdminPanel(
                    user, authService, productService, billService, userRepo, this::onLogout
            );
            cards.add(adminPanel, "ADMIN");
            cardLayout.show(cards, "ADMIN");
        } else {
            OrderPanel orderPanel = new OrderPanel(
                    user, productService, billService, this::onLogout
            );
            cards.add(orderPanel, "STAFF");
            cardLayout.show(cards, "STAFF");
        }
    }

    private void onLogout() {
        cardLayout.show(cards, "LOGIN");
    }
}
