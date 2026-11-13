package com.milktea.ui;

import com.milktea.models.*;
import com.milktea.service.BillService;
import com.milktea.service.ProductService;
import com.milktea.util.BillExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class OrderPanel extends JPanel {
    private final User currentUser;
    private final ProductService productService;
    private final BillService billService;
    private final Runnable onLogout;

    // CCột trái
    private final DefaultListModel<Product> productListModel = new DefaultListModel<>();
    private final JList<Product> lstProducts = new JList<>(productListModel);
    private final JComboBox<String> cbCategory = new JComboBox<>(new String[]{"Tất cả", "Trà sữa", "Trà trái cây", "Cà phê"});
    private final JTextField txtSearch = new JTextField(12);

    private final JComboBox<Size> cbSize = new JComboBox<>(Size.values());
    private final JComboBox<Integer> cbSugar = new JComboBox<>(new Integer[]{100, 70, 50, 30, 0});
    private final JComboBox<Integer> cbIce = new JComboBox<>(new Integer[]{100, 50, 0});
    private final JSpinner spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JTextField txtNote = new JTextField(15);
    private final JPanel pToppings = new JPanel();
    private final List<JCheckBox> toppingCheckboxes = new ArrayList<>();

    // Cột phải (Giỏ hàng & Thanh toán)
    private final List<OrderItem> currentCart = new ArrayList<>();
    private final DefaultTableModel cartTableModel = new DefaultTableModel(
            new String[]{"#", "Tên món", "Chi tiết (Size/Đường/Đá/Topping)", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblCart = new JTable(cartTableModel);

    private final JLabel lblSubtotal = new JLabel("0 đ");
    private final JSpinner spinDiscount = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));
    private final JLabel lblFinalTotal = new JLabel("0 đ");
    private final JTextField txtCustomerCash = new JTextField("0", 10);
    private final JLabel lblChange = new JLabel("0 đ");
    private final JTextField txtCustomerPhone = new JTextField(12);

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0 đ");

    public OrderPanel(User currentUser, ProductService productService, BillService billService, Runnable onLogout) {
        this.currentUser = currentUser;
        this.productService = productService;
        this.billService = billService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        initHeader();
        initContent();

        refreshProductList();
        refreshToppings();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("BOBA SKY - ĐẶT MÓN & BÁN HÀNG");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);

        JPanel pUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pUser.setOpaque(false);

        JLabel lblInfo = new JLabel("Thu ngân: " + currentUser.getFullName() + " (" + currentUser.getRole().getTitle() + ")");
        lblInfo.setFont(UITheme.FONT_BOLD);

        JButton btnLogout = UITheme.createSecondaryButton("Đăng xuất");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
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

    private void initContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setResizeWeight(0.48);
        splitPane.setDividerSize(6);
        add(splitPane, BorderLayout.CENTER);
    }

    // --- CỘT TRÁI: MENU & TÙY CHỌN PHA CHẾ ---
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 15, 10, 10));

        // Thanh lọc & tìm kiếm món
        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pFilter.setBackground(UITheme.CARD_BG);
        pFilter.setBorder(UITheme.createCardBorder());

        pFilter.add(new JLabel("Danh mục:"));
        cbCategory.setFont(UITheme.FONT_REGULAR);
        cbCategory.addActionListener(e -> refreshProductList());
        pFilter.add(cbCategory);

        pFilter.add(new JLabel("Tìm:"));
        txtSearch.setFont(UITheme.FONT_REGULAR);
        txtSearch.addActionListener(e -> refreshProductList());
        JButton btnSearch = UITheme.createSecondaryButton("Lọc");
        btnSearch.addActionListener(e -> refreshProductList());
        pFilter.add(txtSearch);
        pFilter.add(btnSearch);

        panel.add(pFilter, BorderLayout.NORTH);

        // Danh sách món
        lstProducts.setFont(UITheme.FONT_BOLD);
        lstProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstProducts.setFixedCellHeight(34);
        lstProducts.setBackground(Color.WHITE);

        JScrollPane scrollProd = new JScrollPane(lstProducts);
        scrollProd.setBorder(BorderFactory.createTitledBorder("1. Chọn món đồ uống:"));

        // (Size, Đường, Đá, Topping, SL)
        JPanel pCustom = new JPanel();
        pCustom.setLayout(new BoxLayout(pCustom, BoxLayout.Y_AXIS));
        pCustom.setBackground(UITheme.CARD_BG);
        pCustom.setBorder(BorderFactory.createTitledBorder("2. Tùy chọn pha chế & Topping:"));

        // Hàng Size, Đường, Đá
        JPanel rowSpecs = new JPanel(new GridLayout(1, 3, 10, 0));
        rowSpecs.setOpaque(false);

        JPanel pSize = new JPanel(new BorderLayout(0, 4)); pSize.setOpaque(false);
        pSize.add(new JLabel("Size ly:"), BorderLayout.NORTH);
        pSize.add(cbSize, BorderLayout.CENTER);

        JPanel pSugar = new JPanel(new BorderLayout(0, 4)); pSugar.setOpaque(false);
        pSugar.add(new JLabel("Mức đường:"), BorderLayout.NORTH);
        pSugar.add(cbSugar, BorderLayout.CENTER);

        JPanel pIce = new JPanel(new BorderLayout(0, 4)); pIce.setOpaque(false);
        pIce.add(new JLabel("Mức đá:"), BorderLayout.NORTH);
        pIce.add(cbIce, BorderLayout.CENTER);

        rowSpecs.add(pSize);
        rowSpecs.add(pSugar);
        rowSpecs.add(pIce);
        pCustom.add(rowSpecs);
        pCustom.add(Box.createVerticalStrut(10));

        // Danh sách Topping
        pToppings.setLayout(new GridLayout(0, 2, 8, 4));
        pToppings.setOpaque(false);
        JScrollPane scrollTop = new JScrollPane(pToppings);
        scrollTop.setPreferredSize(new Dimension(300, 110));
        scrollTop.setBorder(BorderFactory.createTitledBorder("Chọn Topping thêm:"));
        pCustom.add(scrollTop);
        pCustom.add(Box.createVerticalStrut(10));

        // Số lượng + Ghi chú + Nút thêm
        JPanel rowAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowAdd.setOpaque(false);
        rowAdd.add(new JLabel("Số lượng:"));
        spinQuantity.setPreferredSize(new Dimension(60, 32));
        rowAdd.add(spinQuantity);

        rowAdd.add(new JLabel("Ghi chú:"));
        txtNote.setPreferredSize(new Dimension(140, 32));
        rowAdd.add(txtNote);

        JButton btnAddToCart = UITheme.createPrimaryButton("Thêm vào giỏ");
        btnAddToCart.addActionListener(e -> addItemToCart());
        rowAdd.add(btnAddToCart);

        pCustom.add(rowAdd);

        // Ghép giữa (danh sách món) và dưới (tùy biến)
        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setOpaque(false);
        pCenter.add(scrollProd, BorderLayout.CENTER);
        pCenter.add(pCustom, BorderLayout.SOUTH);

        panel.add(pCenter, BorderLayout.CENTER);
        return panel;
    }

    // --- CỘT PHẢI: GIỎ HÀNG & THANH TOÁN ---
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 10, 10, 15));

        // Bảng giỏ hàng
        tblCart.setFont(UITheme.FONT_REGULAR);
        tblCart.setRowHeight(28);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(210);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(40);
        tblCart.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblCart.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(BorderFactory.createTitledBorder("Đơn hàng hiện tại:"));

        // Nút chỉnh giỏ hàng
        JPanel pCartBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        pCartBtns.setOpaque(false);

        JButton btnInc = UITheme.createSecondaryButton("+1");
        btnInc.addActionListener(e -> changeQuantitySelected(1));

        JButton btnDec = UITheme.createSecondaryButton("-1");
        btnDec.addActionListener(e -> changeQuantitySelected(-1));

        JButton btnRemove = UITheme.createSecondaryButton("Xóa món");
        btnRemove.addActionListener(e -> removeSelectedCartItem());

        JButton btnClear = UITheme.createSecondaryButton("Xóa tất cả");
        btnClear.addActionListener(e -> clearCart());

        pCartBtns.add(btnInc);
        pCartBtns.add(btnDec);
        pCartBtns.add(btnRemove);
        pCartBtns.add(btnClear);

        // Khung tính tiền & Checkout
        JPanel pCheckout = new JPanel();
        pCheckout.setLayout(new BoxLayout(pCheckout, BoxLayout.Y_AXIS));
        pCheckout.setBackground(UITheme.CARD_BG);
        pCheckout.setBorder(UITheme.createCardBorder());

        // Hàng tiền hàng & giảm giá
        JPanel pRows = new JPanel(new GridLayout(5, 2, 10, 8));
        pRows.setOpaque(false);

        pRows.add(new JLabel("Tiền hàng:"));
        lblSubtotal.setFont(UITheme.FONT_BOLD);
        pRows.add(lblSubtotal);

        pRows.add(new JLabel("Giảm giá (%):"));
        spinDiscount.addChangeListener(e -> updatePaymentSummary());
        pRows.add(spinDiscount);

        pRows.add(new JLabel("TỔNG CẦN THANH TOÁN:"));
        lblFinalTotal.setFont(UITheme.FONT_BIG_NUMBER);
        lblFinalTotal.setForeground(UITheme.PRIMARY);
        pRows.add(lblFinalTotal);

        pRows.add(new JLabel("Tiền khách đưa:"));
        txtCustomerCash.setFont(UITheme.FONT_BOLD);
        txtCustomerCash.addActionListener(e -> updatePaymentSummary());
        pRows.add(txtCustomerCash);

        pRows.add(new JLabel("Tiền thối lại:"));
        lblChange.setFont(UITheme.FONT_BOLD);
        lblChange.setForeground(UITheme.ACCENT_GREEN);
        pRows.add(lblChange);

        pCheckout.add(pRows);
        pCheckout.add(Box.createVerticalStrut(12));

        // Hàng SĐT khách + Nút thanh toán lớn
        JPanel pPhone = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pPhone.setOpaque(false);
        pPhone.add(new JLabel("SĐT khách (nếu có):"));
        pPhone.add(txtCustomerPhone);
        pCheckout.add(pPhone);
        pCheckout.add(Box.createVerticalStrut(12));

        JButton btnPay = UITheme.createPrimaryButton("THANH TOÁN & IN HÓA ĐƠN");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnPay.addActionListener(e -> processCheckout());
        pCheckout.add(btnPay);

        // Ráp layout
        JPanel pCenter = new JPanel(new BorderLayout(0, 5));
        pCenter.setOpaque(false);
        pCenter.add(scrollCart, BorderLayout.CENTER);
        pCenter.add(pCartBtns, BorderLayout.SOUTH);

        panel.add(pCenter, BorderLayout.CENTER);
        panel.add(pCheckout, BorderLayout.SOUTH);

        return panel;
    }

    // --- XỬ LÝ ---
    public void refreshProductList() {
        String cat = (String) cbCategory.getSelectedItem();
        String keyword = txtSearch.getText().trim().toLowerCase();

        productListModel.clear();
        List<Product> products = productService.getProductsByCategory(cat);
        for (Product p : products) {
            if (keyword.isEmpty() || p.getName().toLowerCase().contains(keyword)) {
                productListModel.addElement(p);
            }
        }
    }

    public void refreshToppings() {
        pToppings.removeAll();
        toppingCheckboxes.clear();

        List<Topping> toppings = productService.getAvailableToppings();
        for (Topping t : toppings) {
            JCheckBox chk = new JCheckBox(String.format("%s (+%,.0f)", t.getName(), t.getPrice()));
            chk.putClientProperty("TOPPING_DATA", t);
            chk.setFont(UITheme.FONT_REGULAR);
            chk.setOpaque(false);
            toppingCheckboxes.add(chk);
            pToppings.add(chk);
        }
        pToppings.revalidate();
        pToppings.repaint();
    }

    private void addItemToCart() {
        Product selectedProduct = lstProducts.getSelectedValue();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 món đồ uống từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Size size = (Size) cbSize.getSelectedItem();
        int sugar = (Integer) cbSugar.getSelectedItem();
        int ice = (Integer) cbIce.getSelectedItem();
        int qty = (Integer) spinQuantity.getValue();
        String note = txtNote.getText().trim();

        List<Topping> selectedToppings = toppingCheckboxes.stream()
                .filter(JCheckBox::isSelected)
                .map(chk -> (Topping) chk.getClientProperty("TOPPING_DATA"))
                .collect(Collectors.toList());

        OrderItem item = new OrderItem(selectedProduct, size, selectedToppings, sugar, ice, qty, note);
        currentCart.add(item);

        // Reset các lựa chọn
        toppingCheckboxes.forEach(chk -> chk.setSelected(false));
        spinQuantity.setValue(1);
        txtNote.setText("");

        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (int i = 0; i < currentCart.size(); i++) {
            OrderItem it = currentCart.get(i);
            cartTableModel.addRow(new Object[]{
                    (i + 1),
                    it.getProduct().getName(),
                    it.getCustomizationSummary(),
                    it.getQuantity(),
                    moneyFormat.format(it.getUnitPrice()),
                    moneyFormat.format(it.getTotalPrice())
            });
        }
        updatePaymentSummary();
    }

    private void updatePaymentSummary() {
        double subtotal = currentCart.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        int discountPct = (Integer) spinDiscount.getValue();
        double discountAmount = subtotal * (discountPct / 100.0);
        double finalTotal = Math.max(0.0, subtotal - discountAmount);

        lblSubtotal.setText(moneyFormat.format(subtotal));
        lblFinalTotal.setText(moneyFormat.format(finalTotal));

        // Tính tiền thối
        try {
            String cashStr = txtCustomerCash.getText().replaceAll("[^0-9]", "").trim();
            double customerCash = cashStr.isEmpty() ? 0.0 : Double.parseDouble(cashStr);
            double change = Math.max(0.0, customerCash - finalTotal);
            lblChange.setText(moneyFormat.format(change));
        } catch (Exception ignored) {
            lblChange.setText("0 đ");
        }
    }

    private void changeQuantitySelected(int delta) {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= currentCart.size()) return;

        OrderItem item = currentCart.get(row);
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            currentCart.remove(row);
        } else {
            item.setQuantity(newQty);
        }
        refreshCartTable();
    }

    private void removeSelectedCartItem() {
        int row = tblCart.getSelectedRow();
        if (row >= 0 && row < currentCart.size()) {
            currentCart.remove(row);
            refreshCartTable();
        }
    }

    private void clearCart() {
        if (currentCart.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn muốn xóa toàn bộ giỏ hàng?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentCart.clear();
            refreshCartTable();
        }
    }

    private void processCheckout() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn món trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updatePaymentSummary();

        double subtotal = currentCart.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        int discountPct = (Integer) spinDiscount.getValue();
        double finalTotal = subtotal * (1 - discountPct / 100.0);

        String cashStr = txtCustomerCash.getText().replaceAll("[^0-9]", "").trim();
        double customerCash = cashStr.isEmpty() ? 0.0 : Double.parseDouble(cashStr);

        if (customerCash < finalTotal) {
            int opt = JOptionPane.showConfirmDialog(this,
                    String.format("Tiền khách đưa (%,.0f đ) chưa đủ tổng đơn (%,.0f đ)!\nBạn có muốn tự động nhận đủ %,.0f đ không?",
                            customerCash, finalTotal, finalTotal),
                    "Thiếu tiền", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                customerCash = finalTotal;
                txtCustomerCash.setText(String.format("%.0f", customerCash));
            } else {
                return;
            }
        }

        // Tạo hóa đơn
        String billId = "HD" + System.currentTimeMillis() % 1000000;
        Bill bill = new Bill(billId, currentUser.getUsername(), currentUser.getFullName());
        bill.setItems(new ArrayList<>(currentCart));
        bill.setDiscountPercent(discountPct);
        bill.setCustomerCash(customerCash);
        bill.setCustomerPhone(txtCustomerPhone.getText().trim());

        // Lưu vào Storage
        boolean saved = billService.createBill(bill);
        if (saved) {
            // Tự động xuất file text bill
            File billFile = new File("bill-" + bill.getId() + ".txt");
            BillExporter.exportToFile(bill, billFile);

            // Hiển thị dialog hóa đơn
            String billContent = BillExporter.generateBillText(bill);
            JTextArea area = new JTextArea(billContent);
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);

            JOptionPane.showMessageDialog(this,
                    new JScrollPane(area),
                    "Thanh toán thành công! (Đã lưu file: " + billFile.getName() + ")",
                    JOptionPane.INFORMATION_MESSAGE);

            // Dọn sạch giỏ hàng để bắt đầu đơn mới
            currentCart.clear();
            spinDiscount.setValue(0);
            txtCustomerCash.setText("0");
            txtCustomerPhone.setText("");
            refreshCartTable();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
