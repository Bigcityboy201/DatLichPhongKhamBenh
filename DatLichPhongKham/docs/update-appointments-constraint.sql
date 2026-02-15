-- Script để cập nhật constraint cho bảng appointments
-- Chạy script này trong MySQL để cập nhật constraint cho phép status từ 0-7

-- Xóa constraint cũ nếu tồn tại
ALTER TABLE appointments DROP CHECK IF EXISTS appointments_chk_1;

-- Thêm constraint mới cho phép status từ 0-7
-- (PENDING=0, CONFIRMED=1, CANCELLED=2, COMPLETED=3, AWAITING_DEPOSIT=4, DEPOSIT_PAID=5, CANCELLED_REFUND=6, CANCELLED_NO_REFUND=7)
ALTER TABLE appointments ADD CONSTRAINT appointments_chk_1 CHECK (status BETWEEN 0 AND 7);

