# 🧋 Boba Sky - Hệ Thống Quản Lý & Bán Trà Sữa (Java Core POS)

Dự án ứng dụng Desktop quản lý bán hàng (POS) chuyên biệt cho chuỗi cửa hàng trà sữa, được xây dựng hoàn toàn bằng **Java Core** và giao diện **Java Swing/AWT**, thiết kế theo kiến trúc chuẩn 3 lớp (3-Tier Layered Architecture).

---

##  Điểm nổi bật & Kỹ thuật Java Core 

* **Ngôn ngữ**:Java thuần (JDK 8+)
* **Lập trình Hướng đối tượng (OOP)**:
  * **Đóng gói (Encapsulation)**: Kiểm soát trạng thái đơn hàng, dữ liệu sản phẩm, phân quyền người dùng.
  * **Trừu tượng hóa & Kế thừa**: Thiết kế mô hình dữ liệu linh hoạt (`Size`, `Product`, `Topping`, `OrderItem`, `Bill`).
  * **Đa hình (Polymorphism)**: Tùy biến logic tính tiền ly nước linh hoạt: `Giá = Giá món gốc + Phụ thu Size + Tổng tiền các Topping`.
* **Cấu trúc dữ liệu & Java Collections**:
  * `Map<String, User>`: Tra cứu và xác thực tài khoản tức thời O(1).
  * `List<Product>`, `List<Topping>`: Quản lý danh mục menu và topping linh hoạt.
  * `List<OrderItem>`, `List<Bill>`: Giỏ hàng động, lưu vết lịch sử hóa đơn.
* **Lưu trữ dữ liệu (Persistence)**:
  * Cơ chế **Object Serialization** (`ObjectInputStream`, `ObjectOutputStream`) lưu trữ dữ liệu bền vững ra các file nhị phân trong thư mục `data/` (`users.dat`, `products.dat`, `toppings.dat`, `bills.dat`).
  * Quản lý tài nguyên sạch sẽ bằng **Try-with-resources**, đảm bảo tự động đóng stream tránh leak bộ nhớ.
* **Giao diện & Trải nghiệm (UI/UX)**:
  * Sử dụng `CardLayout` chuyển đổi màn hình Đăng nhập $\leftrightarrow$ Thu ngân $\leftrightarrow$ Quản trị.
  * Tự động tính tiền thừa, cập nhật tổng thanh toán theo % giảm giá trong thời gian thực.
  * Hỗ trợ xuất hóa đơn ra file text chuẩn định dạng in bill.

---

##  Cấu trúc thư mục mã nguồn

```text
MilkTeaManagement/
│
├── run.bat                          # File click đúp chuột chạy ứng dụng ngay lập tức
├── .project / .classpath            # Cấu hình mở bằng Eclipse IDE
├── data/                            # Thư mục chứa dữ liệu nhị phân (.dat)
│   ├── users.dat
│   ├── products.dat
│   ├── toppings.dat
│   └── bills.dat
│
└── src/com/milktea/
    ├── Main.java                    # Điểm khởi chạy ứng dụng (Entry point)
    │
    ├── models/                      # Các thực thể dữ liệu (Entities)
    │   ├── User.java                # Người dùng & Phân quyền (ADMIN, STAFF)
    │   ├── Size.java                # Enum Size (S, M, L) kèm mức phụ thu
    │   ├── Product.java             # Món đồ uống (Trà sữa, Trà trái cây, Cà phê...)
    │   ├── Topping.java             # Topping kèm theo và giá
    │   ├── OrderItem.java           # 1 ly nước cụ thể (Món + Size + %Đường + %Đá + Toppings + SL)
    │   └── Bill.java                # Hóa đơn thanh toán hoàn chỉnh
    │
    ├── storage/                     # Tầng lưu trữ File I/O
    │   ├── StorageUtils.java        # Helper Generic đọc/ghi file an toàn
    │   ├── UserRepository.java      # Quản lý tài khoản & tự khởi tạo tài khoản mặc định
    │   ├── ProductRepository.java   # Quản lý danh sách đồ uống
    │   ├── ToppingRepository.java   # Quản lý danh sách Topping
    │   └── BillRepository.java      # Lưu trữ lịch sử hóa đơn
    │
    ├── service/                     # Tầng xử lý nghiệp vụ (Business Logic)
    │   ├── AuthService.java         # Đăng nhập, phân quyền, đổi mật khẩu, tạo nhân viên
    │   ├── ProductService.java      # Lọc món theo danh mục, thêm/sửa/xóa món & topping
    │   └── BillService.java         # Tạo đơn hàng, tính doanh thu tổng, doanh thu hôm nay
    │
    ├── util/                        # Tiện ích bổ trợ
    │   └── BillExporter.java        # Định dạng và xuất hóa đơn ra file text (.txt)
    │
    └── ui/                          # Tầng Giao diện người dùng (Swing)
        ├── UITheme.java             # Bảng màu, font chữ, styling nút bấm
        ├── LoginPanel.java          # Màn hình đăng nhập (có nút đăng nhập nhanh test)
        ├── OrderPanel.java          # Màn hình Bán hàng (POS) dành cho Thu ngân
        ├── AdminPanel.java          # Màn hình Quản trị (Menu, Topping, Nhân sự, Doanh thu)
        └── MainFrame.java           # Khung cửa sổ chính điều hướng
```

---

## Tài khoản mặc định

| Vai trò | Tên đăng nhập | Mật khẩu | Chức năng chính |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin` | `admin123` | Quản lý menu đồ uống, topping, nhân viên, xem báo cáo doanh thu |
| **Thu ngân (STAFF)** | `staff` | `staff123` | Bán hàng, chọn size/đường/đá/topping, in hóa đơn cho khách |



---

## Hướng dẫn khởi chạy

### Cách 1: Chạy bằng file `run.bat` (Nhanh nhất trên Windows)
Chỉ cần **click đúp vào file `run.bat`**, hệ thống sẽ tự kiểm tra biên dịch mã nguồn và mở ứng dụng lên ngay lập tức.

### Cách 2: Mở trong IDE (Eclipse, IntelliJ IDEA, VS Code)
* **Eclipse**: Chọn `File` $\rightarrow$ `Open Projects from File System...` $\rightarrow$ Trỏ tới thư mục `MilkTeaManagement`.
* **IntelliJ / VS Code**: Mở thư mục `MilkTeaManagement` và chạy file `src/com/milktea/Main.java`.

### Cách 3: Chạy bằng dòng lệnh (Terminal / PowerShell)
```powershell
cd E:\LunaraCafe\MilkTeaManagement
java -cp bin com.milktea.Main
```
