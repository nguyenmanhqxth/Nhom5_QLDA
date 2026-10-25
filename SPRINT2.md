# Phạm vi và nghiệm thu Sprint 2

Kế hoạch tham chiếu:
https://docs.google.com/spreadsheets/d/12UPgbpz1QtLCc6OpVQ1mdVUYXjgqd3LGI4d4gN6HouA/edit

## Các thay đổi so với project nguồn

- Loại bỏ controller/service/giao diện thống kê, đánh giá và quản lý nhân viên.
- Loại bỏ API và nút trả sách, gia hạn; không hiển thị cảnh báo hạn trả.
- Giới hạn vai trò của phiên bản này ở ADMIN và USER.
- Thêm CRUD thể loại, xác nhận mật khẩu và kiểm tra đăng ký phía server.
- Thêm tìm kiếm/cập nhật độc giả, thông tin thẻ và khóa/mở tài khoản.
- Khóa tài khoản có hiệu lực với phiên đăng nhập đang hoạt động.
- Bắt buộc lý do từ chối, kiểm tra chuyển trạng thái, tồn kho và chống giao lặp.
- Lưu người xác nhận giao và thời điểm xác nhận trên phiếu.
- Kiểm tra chủ sở hữu phiếu khi độc giả xem chi tiết.
- Chuẩn hóa menu và các màn hình cho phạm vi Sprint 2, dùng Bootstrap.
- Dữ liệu demo dùng profile riêng; không chứa mật khẩu MySQL cá nhân.

## Checklist nghiệm thu của nhóm

- [ ] US-01: đăng ký đúng/sai dữ liệu, trùng tài khoản/email, xác nhận mật khẩu.
- [ ] US-06/07: đăng nhập đúng/sai, phân quyền và đăng xuất.
- [ ] US-08: thêm/sửa/xóa sách, thể loại; dữ liệu không hợp lệ; chống xóa khi có liên kết.
- [ ] US-02: tra cứu tên/tác giả, thể loại, không có kết quả, còn/hết sách.
- [ ] US-09: tìm độc giả, cập nhật hồ sơ/thẻ, khóa/mở và kiểm tra lại phiên hiện có.
- [ ] US-03: gửi phiếu hợp lệ, hết sách, quá hạn, trùng yêu cầu và thẻ hết hạn.
- [ ] US-10: duyệt/từ chối; bắt buộc lý do; không duyệt lại phiếu đã xử lý.
- [ ] US-11: phí, giao sách, người xác nhận, thời gian và không giao trùng.
- [ ] Chạy thực tế trên MySQL của nhóm, xác minh giao diện trên trình duyệt/điện thoại.
- [ ] Bổ sung test report, bug log và minh chứng trước khi đánh dấu Done trong kế hoạch.

Kiểm thử tự động hỗ trợ đối chiếu nghiệp vụ; không tự đánh dấu các mục nghiệm thu của nhóm.
H2 không thay thế hoàn toàn kiểm thử khóa đồng thời trên MySQL.

## Kết quả xác minh ngày 25/10/2026

- Maven: 13 test đạt, không lỗi (12 test tích hợp và 1 test khởi động).
- MySQL local: tạo database riêng `quanlythuvien_nhom5` và dữ liệu demo thành công.
- Kiểm tra HTTP thực tế: đăng nhập Admin/Độc giả, xem 6 sách, tạo phiếu, chặn yêu cầu trùng,
  duyệt, giao sách, lưu người xác nhận, trừ tồn đúng một lần và chặn giao lặp.
- Database dùng thử có một phiếu BORROWED từ quy trình kiểm tra trên; phiếu này không nằm trong code seed.
- Chưa chạy kiểm thử tải đồng thời hoặc đánh giá giao diện trên thiết bị di động.
