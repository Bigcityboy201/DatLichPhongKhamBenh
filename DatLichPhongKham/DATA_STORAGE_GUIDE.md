# Hướng dẫn về Nơi Lưu Trữ Dữ Liệu

## 📍 Khi bạn test bằng Postman, dữ liệu được lưu ở đâu?

Khi bạn gửi request từ Postman đến API (ví dụ: `POST http://localhost:8080/api/users`), dữ liệu sẽ được lưu theo quy trình sau:

```
Postman Request
    ↓
Spring Boot App (Container)
    ↓
MySQL Database (Container)
    ↓
Docker Volume (mysql_data)
    ↓
Ổ cứng máy tính của bạn
```

---

## 1. Nơi Lưu Trữ Dữ Liệu

### 1.1. Trong Docker Container

Dữ liệu được lưu trong **MySQL container** tại:

```
/var/lib/mysql
```

Đây là thư mục mặc định của MySQL để lưu trữ:

- Database files (`.ibd`, `.frm`)
- Log files
- Binary logs
- Configuration files

### 1.2. Trên Máy Host (Windows)

Dữ liệu được lưu trong **Docker Volume** có tên:

- **Development**: `datlichphongkham_mysql_data`
- **Production**: `datlichphongkham_mysql_data_prod`

**Vị trí vật lý trên Windows**:

```
C:\ProgramData\Docker\wsl\data\ext4.vhdx
```

hoặc

```
\\wsl$\docker-desktop-data\data\docker\volumes\datlichphongkham_mysql_data\_data
```

**Lưu ý**:

- Đây là file ảnh (virtual disk) của Docker Desktop
- Không nên truy cập trực tiếp vào đây
- Nên sử dụng Docker commands để quản lý

---

## 2. Cách Xem Dữ Liệu

### 2.1. Truy cập MySQL Container

```bash
# Vào MySQL container
docker-compose exec mysql bash

# Hoặc truy cập MySQL CLI trực tiếp
docker-compose exec mysql mysql -u root -p
# Nhập password: quangtruong1 (hoặc password bạn đã set)
```

### 2.2. Xem Databases

```sql
-- Xem danh sách databases
SHOW DATABASES;

-- Chọn database
USE phongkhambenh;

-- Xem danh sách tables
SHOW TABLES;

-- Xem dữ liệu trong một table
SELECT * FROM users;
SELECT * FROM appointments;
SELECT * FROM payments;
```

### 2.3. Xem Dữ Liệu Từ Command Line

```bash
# Xem tất cả users
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT * FROM users;"

# Xem appointments
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT * FROM appointments;"

# Đếm số records
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) FROM users;"
```

---

## 3. Sử Dụng MySQL Workbench hoặc DBeaver

### 3.1. Cấu hình Kết Nối

Nếu bạn muốn dùng GUI tool như MySQL Workbench, bạn cần:

**Bước 1**: Uncomment phần ports trong `docker-compose.yml`:

```yaml
mysql:
  ports:
    - "${MYSQL_PORT:-3307}:3306" # Đổi sang port 3307 để tránh conflict
```

**Bước 2**: Restart container:

```bash
docker-compose down
docker-compose up -d
```

**Bước 3**: Kết nối trong MySQL Workbench:

- **Host**: `localhost` hoặc `127.0.0.1`
- **Port**: `3307` (port bạn đã set)
- **Username**: `root`
- **Password**: `quangtruong1` (hoặc password bạn đã set)
- **Default Schema**: `phongkhambenh`

---

## 4. Backup và Restore Dữ Liệu

### 4.1. Backup Database

```bash
# Backup toàn bộ database
docker-compose exec mysql mysqldump -u root -pquangtruong1 phongkhambenh > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup một table cụ thể
docker-compose exec mysql mysqldump -u root -pquangtruong1 phongkhambenh users > users_backup.sql
```

### 4.2. Restore Database

```bash
# Restore từ file backup
docker-compose exec -T mysql mysql -u root -pquangtruong1 phongkhambenh < backup.sql

# Hoặc restore một table
docker-compose exec -T mysql mysql -u root -pquangtruong1 phongkhambenh < users_backup.sql
```

### 4.3. Export Dữ Liệu Ra CSV

```bash
# Export users table ra CSV
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT * FROM users" | sed 's/\t/,/g' > users.csv
```

---

## 5. Xem Thông Tin Volume

### 5.1. Liệt kê Volumes

```bash
# Xem tất cả volumes
docker volume ls

# Xem chi tiết volume
docker volume inspect datlichphongkham_mysql_data
```

### 5.2. Xem Kích Thước Volume

```bash
# Xem dung lượng sử dụng
docker system df -v
```

### 5.3. Xóa Volume (⚠️ CẢNH BÁO: Sẽ mất tất cả dữ liệu)

```bash
# Dừng containers và xóa volumes
docker-compose down -v

# Hoặc xóa volume riêng lẻ
docker volume rm datlichphongkham_mysql_data
```

---

## 6. Các Bảng Dữ Liệu Chính

Dựa vào cấu trúc ứng dụng, các bảng chính có thể bao gồm:

### 6.1. Users Table

```sql
SELECT * FROM users;
-- Lưu thông tin: username, password, email, full_name, role_id
```

### 6.2. Appointments Table

```sql
SELECT * FROM appointments;
-- Lưu thông tin: user_id, doctor_id, schedule_id, status, appointment_date
```

### 6.3. Payments Table

```sql
SELECT * FROM payments;
-- Lưu thông tin: appointment_id, amount, payment_method, status, transaction_id
```

### 6.4. Doctors Table

```sql
SELECT * FROM doctors;
-- Lưu thông tin: name, specialization, department_id
```

### 6.5. Schedules Table

```sql
SELECT * FROM schedules;
-- Lưu thông tin: doctor_id, day_of_week, start_time, end_time
```

---

## 7. Kiểm Tra Dữ Liệu Sau Khi Test

### 7.1. Script Kiểm Tra Nhanh

Tạo file `check_data.sh`:

```bash
#!/bin/bash
echo "=== Users ==="
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) as total_users FROM users;"

echo "=== Appointments ==="
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) as total_appointments FROM appointments;"

echo "=== Payments ==="
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) as total_payments FROM payments;"
```

Chạy:

```bash
chmod +x check_data.sh
./check_data.sh
```

### 7.2. Xem Logs của App

```bash
# Xem logs để kiểm tra request từ Postman
docker-compose logs app | grep -i "POST\|GET\|PUT\|DELETE"

# Xem logs real-time
docker-compose logs -f app
```

---

## 8. Xóa Dữ Liệu Test

### 8.1. Xóa Dữ Liệu Trong Table

```sql
-- Xóa tất cả users (trừ admin)
DELETE FROM users WHERE role_id != 1;

-- Xóa tất cả appointments
DELETE FROM appointments;

-- Xóa tất cả payments
DELETE FROM payments;

-- Reset auto increment
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE appointments AUTO_INCREMENT = 1;
ALTER TABLE payments AUTO_INCREMENT = 1;
```

### 8.2. Xóa Toàn Bộ Database và Tạo Lại

```bash
# Dừng containers
docker-compose down

# Xóa volume (⚠️ Mất tất cả dữ liệu)
docker volume rm datlichphongkham_mysql_data

# Chạy lại (sẽ tạo database mới)
docker-compose up -d
```

---

## 9. Migration và Schema

### 9.1. Xem Schema của Table

```sql
-- Xem cấu trúc table
DESCRIBE users;
DESCRIBE appointments;

-- Hoặc
SHOW CREATE TABLE users;
```

### 9.2. Xem Indexes

```sql
SHOW INDEXES FROM users;
```

---

## 10. Monitoring và Performance

### 10.1. Xem Process List

```sql
SHOW PROCESSLIST;
```

### 10.2. Xem Database Size

```sql
SELECT
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'phongkhambenh'
GROUP BY table_schema;
```

### 10.3. Xem Table Sizes

```sql
SELECT
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'phongkhambenh'
ORDER BY (data_length + index_length) DESC;
```

---

## 11. Troubleshooting

### 11.1. Không thấy dữ liệu sau khi POST

**Kiểm tra**:

1. Xem logs của app: `docker-compose logs app`
2. Kiểm tra database connection: `docker-compose exec mysql mysql -u root -p`
3. Kiểm tra transaction đã commit chưa
4. Kiểm tra có lỗi validation không

### 11.2. Dữ liệu bị mất sau khi restart

**Nguyên nhân**: Volume chưa được mount đúng

**Giải pháp**:

```bash
# Kiểm tra volume
docker volume ls
docker volume inspect datlichphongkham_mysql_data

# Đảm bảo volume được mount trong docker-compose.yml
```

### 11.3. Không thể kết nối MySQL từ host

**Giải pháp**: Uncomment ports trong docker-compose.yml và đổi port

---

## 12. Tóm Tắt

| Hành động                   | Vị trí lưu trữ                               |
| --------------------------- | -------------------------------------------- |
| **POST request từ Postman** | → Spring Boot App                            |
| **App xử lý và lưu**        | → MySQL Database trong container             |
| **Database files**          | → `/var/lib/mysql` trong container           |
| **Docker Volume**           | → `datlichphongkham_mysql_data`              |
| **Vật lý trên máy**         | → `C:\ProgramData\Docker\wsl\data\ext4.vhdx` |

**Lưu ý quan trọng**:

- ✅ Dữ liệu **PERSISTENT** - không mất khi restart container
- ✅ Dữ liệu **MẤT** khi chạy `docker-compose down -v`
- ✅ Nên **backup** thường xuyên trước khi xóa volume
- ✅ Có thể truy cập qua MySQL CLI hoặc GUI tools

---

## 13. Quick Reference

```bash
# Xem dữ liệu users
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh -e "SELECT * FROM users;"

# Backup
docker-compose exec mysql mysqldump -u root -pquangtruong1 phongkhambenh > backup.sql

# Restore
docker-compose exec -T mysql mysql -u root -pquangtruong1 phongkhambenh < backup.sql

# Vào MySQL CLI
docker-compose exec mysql mysql -u root -p

# Xem logs app
docker-compose logs -f app
```

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0

