package com.milktea.ui;

import com.milktea.models.*;
import com.milktea.service.AuthService;
import com.milktea.service.BillService;
import com.milktea.service.ProductService;
import com.milktea.storage.UserRepository;
import com.milktea.util.BillExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("serial")
public class AdminPanel extends JPanel {
    private final User adminUser;
    private final AuthService authService;
    private final ProductService productService;
    private final BillService billService;
    private final UserRepository userRepo;
    private final Runnable onLogout;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Table Models
    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[]{"ID", "Tên đồ uống", "Danh mục", "Giá size S", "Trạng thái"}, 0);
    private final DefaultTableModel toppingModel = new DefaultTableModel(
            new String[]{"ID", "Tên Topping", "Giá bán", "Trạng thái"}, 0);
    private final DefaultTableModel userModel = new DefaultTableModel(
            new String[]{"Tên đăng nhập", "Họ và tên", "Số điện thoại", "Vai trò", "Trạng thái"}, 0);
    private final DefaultTableModel billModel = new DefaultTableModel(
            new String[]{"Mã HĐ", "Thu ngân", "Thời gian", "Số lượng ly", "Giảm giá (%)", "Tổng tiền"}, 0);

    public AdminPanel(User adminUser, AuthService authService, ProductService productService,
                      BillService billService, UserRepository userRepo, Runnable onLogout) {
        this.adminUser = adminUser;
        this.authService = authService;
        this.productService = productService;
        this.billService = billService;
        this.userRepo = userRepo;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        initHeader();
        initTabs();

        refreshProducts();
        refreshToppings();
        refreshUsers();
        refreshBills();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("BOBA SKY - TRANG QUẢN TRỊ ADMIN");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        JPanel pUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pUser.setOpaque(false);

        JLabel lblInfo = new JLabel("Quản trị: " + adminUser.getFullName());
        lblInfo.setFont(UITheme.FONT_BOLD);

        JButton btnLogout = UITheme.createSecondaryButton("Đăng xuất");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                onLogout.run();
            }
        });

        pUser.add(lblInfo);
        pUser.add(btnLogout);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(pUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("Quản lý Menu & Topping", createMenuTab());
        tabbedPane.addTab("Quản lý Nhân viên", createUserTab());
        tabbedPane.addTab("Lịch sử Hóa đơn", createBillTab());
        tabbedPane.addTab("Báo cáo Thống kê", createStatsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }


    // TAB 1: MENU & TOPPING

    private JPanel createMenuTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Nửa trái: Sản phẩm đồ uống
        JPanel pProd = new JPanel(new BorderLayout(0, 8));
        pProd.setBackground(UITheme.CARD_BG);
        pProd.setBorder(BorderFactory.createTitledBorder("Danh sách Đồ uống / Trà sữa"));

        JTable tblProd = new JTable(productModel);
        tblProd.setRowHeight(26);
        pProd.add(new JScrollPane(tblProd), BorderLayout.CENTER);

        JPanel pProdBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton btnAddProd = UITheme.createPrimaryButton("Thêm món");
        JButton btnEditProd = UITheme.createSecondaryButton("Sửa món");
        JButton btnToggleProd = UITheme.createSecondaryButton("Đổi trạng thái");
        JButton btnDelProd = UITheme.createSecondaryButton("Xóa món");

        btnAddProd.addActionListener(e -> handleAddProduct());
        btnEditProd.addActionListener(e -> handleEditProduct(tblProd));
        btnToggleProd.addActionListener(e -> handleToggleProduct(tblProd));
        btnDelProd.addActionListener(e -> handleDeleteProduct(tblProd));

        pProdBtns.add(btnAddProd);
        pProdBtns.add(btnEditProd);
        pProdBtns.add(btnToggleProd);
        pProdBtns.add(btnDelProd);
        pProd.add(pProdBtns, BorderLayout.SOUTH);

        // Nửa phải: Topping
        JPanel pTop = new JPanel(new BorderLayout(0, 8));
        pTop.setBackground(UITheme.CARD_BG);
        pTop.setBorder(BorderFactory.createTitledBorder("Danh sách Topping"));

        JTable tblTop = new JTable(toppingModel);
        tblTop.setRowHeight(26);
        pTop.add(new JScrollPane(tblTop), BorderLayout.CENTER);

        JPanel pTopBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton btnAddTop = UITheme.createPrimaryButton("Thêm Topping");
        JButton btnEditTop = UITheme.createSecondaryButton("Sửa giá");
        JButton btnDelTop = UITheme.createSecondaryButton("Xóa Topping");

        btnAddTop.addActionListener(e -> handleAddTopping());
        btnEditTop.addActionListener(e -> handleEditTopping(tblTop));
        btnDelTop.addActionListener(e -> handleDeleteTopping(tblTop));

        pTopBtns.add(btnAddTop);
        pTopBtns.add(btnEditTop);
        pTopBtns.add(btnDelTop);
        pTop.add(pTopBtns, BorderLayout.SOUTH);

        panel.add(pProd);
        panel.add(pTop);
        return panel;
    }

    private void refreshProducts() {
        productModel.setRowCount(0);
        List<Product> list = productService.getAllProducts();
        for (Product p : list) {
            productModel.addRow(new Object[]{
                    p.getId().substring(0, Math.min(8, p.getId().length())),
                    p.getName(),
                    p.getCategory(),
                    moneyFormat.format(p.getBasePrice()),
                    p.isAvailable() ? "Đang bán" : "Tạm ngưng"
            });
        }
    }

    private void handleAddProduct() {
        JTextField txtName = new JTextField();
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"Trà sữa", "Trà trái cây", "Cà phê", "Đặc biệt"});
        JTextField txtPrice = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new JLabel("Tên món:")); p.add(txtName);
        p.add(new JLabel("Danh mục:")); p.add(cbCat);
        p.add(new JLabel("Giá bán Size S (VNĐ):")); p.add(txtPrice);

        int res = JOptionPane.showConfirmDialog(this, p, "Thêm món mới", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                String cat = (String) cbCat.getSelectedItem();
                double price = Double.parseDouble(txtPrice.getText().replaceAll("[^0-9]", "").trim());
                if (name.isEmpty() || price <= 0) throw new IllegalArgumentException();

                productService.addProduct(name, cat, price);
                refreshProducts();
                JOptionPane.showMessageDialog(this, "Đã thêm món thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Thông tin không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditProduct(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 món để sửa!");
            return;
        }

        List<Product> list = productService.getAllProducts();
        Product p = list.get(row);

        JTextField txtName = new JTextField(p.getName());
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"Trà sữa", "Trà trái cây", "Cà phê", "Đặc biệt"});
        cbCat.setSelectedItem(p.getCategory());
        JTextField txtPrice = new JTextField(String.format("%.0f", p.getBasePrice()));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Tên món:")); panel.add(txtName);
        panel.add(new JLabel("Danh mục:")); panel.add(cbCat);
        panel.add(new JLabel("Giá bán Size S:")); panel.add(txtPrice);

        int res = JOptionPane.showConfirmDialog(this, panel, "Sửa món đồ uống", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                p.setName(txtName.getText().trim());
                p.setCategory((String) cbCat.getSelectedItem());
                p.setBasePrice(Double.parseDouble(txtPrice.getText().replaceAll("[^0-9]", "").trim()));
                productService.updateProduct(p);
                refreshProducts();
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Giá nhập không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleProduct(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Product p = productService.getAllProducts().get(row);
        p.setAvailable(!p.isAvailable());
        productService.updateProduct(p);
        refreshProducts();
    }

    private void handleDeleteProduct(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Product p = productService.getAllProducts().get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa món: " + p.getName() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            productService.deleteProduct(p.getId());
            refreshProducts();
        }
    }

    private void refreshToppings() {
        toppingModel.setRowCount(0);
        List<Topping> list = productService.getAllToppings();
        for (Topping t : list) {
            toppingModel.addRow(new Object[]{
                    t.getId().substring(0, Math.min(8, t.getId().length())),
                    t.getName(),
                    moneyFormat.format(t.getPrice()),
                    t.isAvailable() ? "Còn hàng" : "Hết hàng"
            });
        }
    }

    private void handleAddTopping() {
        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new JLabel("Tên Topping:")); p.add(txtName);
        p.add(new JLabel("Giá Topping (VNĐ):")); p.add(txtPrice);

        int res = JOptionPane.showConfirmDialog(this, p, "Thêm Topping mới", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                double price = Double.parseDouble(txtPrice.getText().replaceAll("[^0-9]", "").trim());
                productService.addTopping(name, price);
                refreshToppings();
                JOptionPane.showMessageDialog(this, "Đã thêm Topping!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Thông tin Topping không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditTopping(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Topping t = productService.getAllToppings().get(row);

        String newPriceStr = JOptionPane.showInputDialog(this, "Giá mới cho " + t.getName() + ":", String.format("%.0f", t.getPrice()));
        if (newPriceStr != null) {
            try {
                double p = Double.parseDouble(newPriceStr.replaceAll("[^0-9]", "").trim());
                t.setPrice(p);
                productService.updateTopping(t);
                refreshToppings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteTopping(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Topping t = productService.getAllToppings().get(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa Topping: " + t.getName() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            productService.deleteTopping(t.getId());
            refreshToppings();
        }
    }


    // TAB 2: NHÂN VIÊN

    private JPanel createUserTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTable tblUsers = new JTable(userModel);
        tblUsers.setRowHeight(28);
        panel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton btnAdd = UITheme.createPrimaryButton("Thêm nhân viên");
        JButton btnToggleActive = UITheme.createSecondaryButton("Khóa / Mở khóa");
        JButton btnResetPass = UITheme.createSecondaryButton("Đổi mật khẩu");
        JButton btnDelete = UITheme.createSecondaryButton("Xóa tài khoản");

        btnAdd.addActionListener(e -> handleAddUser());
        btnToggleActive.addActionListener(e -> handleToggleUser(tblUsers));
        btnResetPass.addActionListener(e -> handleResetUserPass(tblUsers));
        btnDelete.addActionListener(e -> handleDeleteUser(tblUsers));

        pBtns.add(btnAdd);
        pBtns.add(btnToggleActive);
        pBtns.add(btnResetPass);
        pBtns.add(btnDelete);
        panel.add(pBtns, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshUsers() {
        userModel.setRowCount(0);
        for (User u : userRepo.findAll()) {
            userModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getPhone(),
                    u.getRole().getTitle(),
                    u.isActive() ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    private void handleAddUser() {
        JTextField txtU = new JTextField();
        JTextField txtName = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JTextField txtPhone = new JTextField();
        JComboBox<User.Role> cbR = new JComboBox<>(User.Role.values());

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new JLabel("Tên đăng nhập:")); p.add(txtU);
        p.add(new JLabel("Họ và tên:")); p.add(txtName);
        p.add(new JLabel("Mật khẩu:")); p.add(txtP);
        p.add(new JLabel("Số điện thoại:")); p.add(txtPhone);
        p.add(new JLabel("Vai trò:")); p.add(cbR);

        int res = JOptionPane.showConfirmDialog(this, p, "Tạo tài khoản mới", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String u = txtU.getText().trim();
            String name = txtName.getText().trim();
            String pass = new String(txtP.getPassword()).trim();
            String phone = txtPhone.getText().trim();
            User.Role role = (User.Role) cbR.getSelectedItem();

            if (u.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập và mật khẩu không được để trống!");
                return;
            }

            boolean ok = authService.register(u, name, pass, phone, role);
            if (ok) {
                refreshUsers();
                JOptionPane.showMessageDialog(this, "Đã tạo tài khoản thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleUser(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String username = (String) table.getValueAt(row, 0);
        userRepo.findByUsername(username).ifPresent(u -> {
            if (u.getUsername().equals(adminUser.getUsername())) {
                JOptionPane.showMessageDialog(this, "Không thể khóa tài khoản chính bạn đang đăng nhập!");
                return;
            }
            u.setActive(!u.isActive());
            userRepo.update(u);
            refreshUsers();
        });
    }

    private void handleResetUserPass(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String username = (String) table.getValueAt(row, 0);
        userRepo.findByUsername(username).ifPresent(u -> {
            String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới cho " + u.getUsername() + ":");
            if (newPass != null && !newPass.trim().isEmpty()) {
                u.setPassword(newPass.trim());
                userRepo.update(u);
                JOptionPane.showMessageDialog(this, "Đã cập nhật mật khẩu mới!");
            }
        });
    }

    private void handleDeleteUser(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String username = (String) table.getValueAt(row, 0);
        if (username.equals(adminUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "Không thể xóa chính tài khoản bạn đang đăng nhập!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa tài khoản: " + username + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            userRepo.delete(username);
            refreshUsers();
        }
    }


    // TAB 3: LỊCH SỬ HÓA ĐƠN

    private JPanel createBillTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTable tblBills = new JTable(billModel);
        tblBills.setRowHeight(28);
        panel.add(new JScrollPane(tblBills), BorderLayout.CENTER);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton btnView = UITheme.createPrimaryButton("Xem chi tiết hóa đơn");
        JButton btnExport = UITheme.createSecondaryButton("Xuất file TXT");
        JButton btnRefresh = UITheme.createSecondaryButton("Làm mới danh sách");

        btnView.addActionListener(e -> handleViewBill(tblBills));
        btnExport.addActionListener(e -> handleExportBill(tblBills));
        btnRefresh.addActionListener(e -> refreshBills());

        pBtns.add(btnView);
        pBtns.add(btnExport);
        pBtns.add(btnRefresh);
        panel.add(pBtns, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshBills() {
        billModel.setRowCount(0);
        List<Bill> bills = billService.getAllBills();
        for (Bill b : bills) {
            billModel.addRow(new Object[]{
                    b.getId(),
                    b.getCashierName(),
                    b.getCreatedAt().format(dateFormat),
                    b.getTotalQuantity() + " ly",
                    String.format("%.0f%%", b.getDiscountPercent()),
                    moneyFormat.format(b.getFinalTotal())
            });
        }
    }

    private void handleViewBill(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 hóa đơn để xem!");
            return;
        }

        String billId = (String) table.getValueAt(row, 0);
        billService.getBillById(billId).ifPresent(bill -> {
            String text = BillExporter.generateBillText(bill);
            JTextArea area = new JTextArea(text);
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Chi tiết hóa đơn " + billId, JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void handleExportBill(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String billId = (String) table.getValueAt(row, 0);
        billService.getBillById(billId).ifPresent(bill -> {
            File file = new File("bill-" + bill.getId() + ".txt");
            boolean ok = BillExporter.exportToFile(bill, file);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xuất ra file: " + file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    // TAB 4: THỐNG KÊ DOANH THU

    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Các thẻ tóm tắt (Cards)
        JPanel pCards = new JPanel(new GridLayout(1, 3, 15, 0));
        pCards.setOpaque(false);

        List<Bill> bills = billService.getAllBills();
        double totalRev = billService.calculateTotalRevenue();
        double todayRev = billService.calculateTodayRevenue();
        int totalBills = bills.size();

        pCards.add(createStatCard("TỔNG DOANH THU", moneyFormat.format(totalRev), UITheme.PRIMARY));
        pCards.add(createStatCard("DOANH THU HÔM NAY", moneyFormat.format(todayRev), UITheme.ACCENT_GREEN));
        pCards.add(createStatCard("TỔNG HÓA ĐƠN ĐÃ BÁN", totalBills + " đơn", UITheme.SECONDARY));

        panel.add(pCards, BorderLayout.NORTH);

        // Mô tả chi tiết
        JTextArea areaSummary = new JTextArea();
        areaSummary.setEditable(false);
        areaSummary.setFont(UITheme.FONT_REGULAR);
        areaSummary.setBorder(new EmptyBorder(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("=== TỔNG QUAN KINH DOANH TIỆM TRÀ SỮA ===\n\n");
        sb.append(String.format("- Tổng số hóa đơn đã phát hành : %d đơn\n", totalBills));
        sb.append(String.format("- Tổng doanh thu tích lũy        : %s\n", moneyFormat.format(totalRev)));
        sb.append(String.format("- Doanh thu trong ngày hôm nay  : %s\n", moneyFormat.format(todayRev)));
        if (totalBills > 0) {
            sb.append(String.format("- Giá trị trung bình mỗi đơn   : %s\n", moneyFormat.format(totalRev / totalBills)));
        }
        sb.append("\n(Hệ thống tự động lưu trữ và đồng bộ vào file nhị phân Serialization trong thư mục data/)");
        areaSummary.setText(sb.toString());

        panel.add(new JScrollPane(areaSummary), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(UITheme.createCardBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(UITheme.FONT_BIG_NUMBER);
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
}
