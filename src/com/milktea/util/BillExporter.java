package com.milktea.util;

import com.milktea.models.Bill;
import com.milktea.models.OrderItem;
import com.milktea.models.Topping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public class BillExporter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String generateBillText(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("               TIỆM TRÀ SỮA BOBA SKY             \n");
        sb.append("         Đ/c: 123 Đường Hoa Sữa, Quận 1, TP.HCM   \n");
        sb.append("               Hotline: 1900 6868                \n");
        sb.append("==================================================\n");
        sb.append(String.format(" Mã hóa đơn : %s\n", bill.getId()));
        sb.append(String.format(" Thu ngân   : %s (%s)\n", bill.getCashierName(), bill.getCashierUsername()));
        sb.append(String.format(" Thời gian  : %s\n", bill.getCreatedAt().format(DATE_FORMAT)));
        if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isEmpty()) {
            sb.append(String.format(" Khách hàng : %s\n", bill.getCustomerPhone()));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format(" %-24s %4s %8s %10s\n", "Tên món", "SL", "Đơn giá", "T.Tiền"));
        sb.append("--------------------------------------------------\n");

        for (OrderItem item : bill.getItems()) {
            sb.append(String.format(" %-24s %4d %,8.0f %,10.0f\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));

            // In chi tiết size, đường, đá
            sb.append(String.format("   + Size %s | %d%% Đường, %d%% Đá\n",
                    item.getSize().name(),
                    item.getSugarPercent(),
                    item.getIcePercent()));

            // In topping nếu có
            if (!item.getToppings().isEmpty()) {
                sb.append("   + Topping: ");
                for (int i = 0; i < item.getToppings().size(); i++) {
                    Topping t = item.getToppings().get(i);
                    sb.append(t.getName()).append(String.format(" (%,.0f)", t.getPrice()));
                    if (i < item.getToppings().size() - 1) sb.append(", ");
                }
                sb.append("\n");
            }

            if (item.getNote() != null && !item.getNote().trim().isEmpty()) {
                sb.append(String.format("   * Ghi chú: %s\n", item.getNote().trim()));
            }
        }

        sb.append("--------------------------------------------------\n");
        sb.append(String.format(" Tổng số lượng ly : %d ly\n", bill.getTotalQuantity()));
        sb.append(String.format(" Tiền hàng        : %,.0f đ\n", bill.getSubtotal()));
        if (bill.getDiscountPercent() > 0) {
            sb.append(String.format(" Giảm giá (%.0f%%)   : -%,.0f đ\n",
                    bill.getDiscountPercent(), bill.getDiscountAmount()));
        }
        sb.append(String.format(" TỔNG THANH TOÁN  : %,.0f đ\n", bill.getFinalTotal()));
        sb.append(String.format(" Khách đưa        : %,.0f đ\n", bill.getCustomerCash()));
        sb.append(String.format(" Tiền thừa        : %,.0f đ\n", bill.getChangeAmount()));
        sb.append("==================================================\n");
        sb.append("      CẢM ƠN QUÝ KHÁCH - HẸN GẶP LẠI!         \n");
        sb.append("==================================================\n");

        return sb.toString();
    }

    public static boolean exportToFile(Bill bill, File targetFile) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            writer.write(generateBillText(bill));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
