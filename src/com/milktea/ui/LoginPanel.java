package com.milktea.ui;

import com.milktea.models.User;
import com.milktea.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("serial")
public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);

    public LoginPanel(AuthService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BACKGROUND);

        initUI();
    }

    private void initUI() {
        // Khung card đăng nhập
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Tiêu đề
        JLabel lblTitle = new JLabel("TIỆM TRÀ SỮA BOBA SKY", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Hệ Thống Quản Lý & Bán Hàng", SwingConstants.CENTER);
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(25));

        // Input Form
        JPanel form = new JPanel(new GridLayout(4, 1, 5, 8));
        form.setOpaque(false);

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(UITheme.FONT_BOLD);
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(260, 36));

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(UITheme.FONT_BOLD);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(260, 36));

        form.add(lblUser);
        form.add(txtUsername);
        form.add(lblPass);
        form.add(txtPassword);

        card.add(form);
        card.add(Box.createVerticalStrut(20));

        // Nút Đăng nhập
        JButton btnLogin = UITheme.createPrimaryButton("ĐĂNG NHẬP HỆ THỐNG");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        // Enter
        txtPassword.addActionListener(e -> performLogin());

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(20));

        // Phím tắt tài khoản mẫu
        JPanel pQuick = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pQuick.setOpaque(false);
        JLabel lblQuick = new JLabel("Đăng nhập nhanh:");
        lblQuick.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblQuick.setForeground(UITheme.TEXT_MUTED);

        JButton btnQuickAdmin = new JButton("Admin");
        btnQuickAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnQuickAdmin.setFocusPainted(false);
        btnQuickAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
        });

        JButton btnQuickStaff = new JButton("Thu Ngân");
        btnQuickStaff.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnQuickStaff.setFocusPainted(false);
        btnQuickStaff.addActionListener(e -> {
            txtUsername.setText("staff");
            txtPassword.setText("staff123");
        });

        pQuick.add(lblQuick);
        pQuick.add(btnQuickAdmin);
        pQuick.add(btnQuickStaff);

        card.add(pQuick);

        add(card);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<User> userOpt = authService.login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            txtPassword.setText("");
            onLoginSuccess.accept(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sai thông tin đăng nhập hoặc tài khoản đang bị khóa!",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
