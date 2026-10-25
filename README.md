# Quản lý thư viện — Nhóm 5 — Sprint 2

Phiên bản giới hạn ở Sprint 1 và Sprint 2 theo kế hoạch của nhóm.
Ngày chuẩn bị: 25/10/2026. Sprint 2 dự kiến kết thúc 01/11/2026;
việc có mã nguồn không thay thế biên bản review hoặc nghiệm thu của nhóm.

## Phạm vi

| Sprint | User Story | Chức năng |
| --- | --- | --- |
| 1 | US-01 | Đăng ký độc giả, xác nhận mật khẩu, tạo thẻ |
| 1 | US-06, US-07 | Đăng nhập, phân quyền Admin/Độc giả, đăng xuất |
| 1 | US-08 | Thêm/sửa/xóa sách và thể loại; chặn xóa dữ liệu đang được sử dụng |
| 1 | US-02 | Tra cứu tên/tác giả, lọc thể loại và còn sách, sắp xếp |
| 2 | US-09 | Tìm độc giả, sửa hồ sơ/thẻ, khóa/mở tài khoản |
| 2 | US-03 | Đăng ký mượn, kiểm tra tồn kho, yêu cầu trùng, hạn thẻ và sách quá hạn |
| 2 | US-10 | Duyệt PENDING → CONFIRMED; từ chối kèm lý do → REJECTED |
| 2 | US-11 | Xác nhận phí và giao sách → BORROWED; giảm tồn, lưu người và thời gian xác nhận |

Chưa bao gồm giao diện/API trả sách, gia hạn, cảnh báo hạn trả, báo cáo thống kê,
đánh giá sách hoặc quản lý nhân viên. Trang quản trị là menu thao tác, chưa có biểu đồ thống kê.
Các trường/trạng thái dữ liệu trả sách và quá hạn được giữ làm nền cho sprint tiếp theo;
kiểm tra sách quá hạn khi đăng ký mượn thuộc tiêu chí Sprint 2.

## Chạy trên Windows PowerShell

Yêu cầu JDK 17 và MySQL 8 đang chạy. Maven Wrapper tự tải Maven và thư viện.

```powershell
cd D:\QLDA\PRJ\Nhom5_QLDA
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'MAT_KHAU_MYSQL_CUA_BAN'
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Database mặc định là `quanlythuvien_nhom5`, tách khỏi database project nguồn.
Tài khoản MySQL cần quyền tạo database và cập nhật bảng; có thể tạo database trước rồi cấp quyền.
Để dùng database khác, đặt biến `DB_URL`. Cấu hình URL mặc định dành cho MySQL trên máy cá nhân.
Không đưa mật khẩu MySQL thật vào source hoặc commit.

Profile `demo` thêm 3 thể loại, 6 sách giả lập và tài khoản độc giả có thẻ.
Chạy lại không tạo trùng dữ liệu mẫu và không ghi đè tài khoản đã tồn tại.
Những lần sau có thể chạy ` .\mvnw.cmd spring-boot:run` mà không cần profile.

| Vai trò | Tài khoản | Mật khẩu ban đầu |
| --- | --- | --- |
| Admin (tạo khi chưa tồn tại) | admin | admin123 |
| Độc giả (profile demo) | reader01 | Reader123456 |

Các tài khoản trên dành cho demo local. Không sử dụng mật khẩu mẫu khi triển khai công khai.

## Màn hình

- Kho sách: http://localhost:8080/books/list
- Đăng nhập: http://localhost:8080/auth/login
- Đăng ký: http://localhost:8080/auth/register
- Trang chủ độc giả: http://localhost:8080/user/home
- Phiếu mượn cá nhân: http://localhost:8080/user/my-borrows
- Hồ sơ cá nhân: http://localhost:8080/user/profile
- Trang quản trị: http://localhost:8080/admin/dashboard
- Quản lý sách: http://localhost:8080/admin/books
- Quản lý thể loại: http://localhost:8080/admin/categories
- Quản lý độc giả: http://localhost:8080/admin/readers
- Xử lý phiếu mượn: http://localhost:8080/admin/borrows

## Quy trình demo

1. Đăng nhập `reader01`, mở chi tiết sách, đăng ký mượn.
2. Đăng xuất; đăng nhập `admin`, vào Phiếu mượn, duyệt phiếu chờ.
3. Kiểm tra phí 70.000 đồng, xác nhận nhận phí và giao sách.
4. Kiểm tra trạng thái BORROWED, tồn kho giảm một, người/thời gian giao được lưu.
5. Đăng nhập lại độc giả để xem phiếu; hạn trả là 14 ngày từ lúc giao sách.

Phí 70.000 đồng và thời hạn 14 ngày kế thừa project nguồn, cần nhóm xác nhận khi nghiệm thu nghiệp vụ.
CDN Bootstrap cần kết nối Internet để tải giao diện.

## Kiểm thử

```powershell
.\mvnw.cmd test
```

Kiểm thử dùng H2 trong bộ nhớ, không đọc/ghi database MySQL cá nhân.
Bao gồm đăng ký và thẻ độc giả, đăng nhập, phân quyền, CSRF, khóa tài khoản,
luồng mượn–duyệt–giao, lý do từ chối, giao lặp, hết sách, quá hạn, hồ sơ và thể loại.
Xem giới hạn xác minh và checklist nghiệm thu tại [SPRINT2.md](SPRINT2.md).

## Nguồn tham khảo

Phát triển từ [Nh-m-4-QLDACNTT](https://github.com/Daoxuanxam/Nh-m-4-QLDACNTT),
mốc nguồn `2e64310`. Giữ package `qltv.com` và tên lớp khởi động gốc để hạn chế thay đổi kỹ thuật.
Đây là bản điều chỉnh phạm vi cho kế hoạch Nhóm 5; không phải lịch sử commit theo từng sprint của dự án nguồn.
