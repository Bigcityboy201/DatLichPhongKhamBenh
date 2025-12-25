# Hướng Dẫn Kết Nối MySQL Workbench Với Docker MySQL

## ✅ Đã Hoàn Thành

Tôi đã cấu hình Docker để expose MySQL port **3307** ra ngoài. Bây giờ bạn có thể kết nối MySQL Workbench với MySQL Docker.

---

## 🔌 Cách Kết Nối MySQL Workbench

### Bước 1: Mở MySQL Workbench

### Bước 2: Tạo Connection Mới

1. Click nút **"+"** (hoặc **"New Connection"**)
2. Điền thông tin sau:

```
Connection Name: Docker MySQL - PhongKhamBenh
Hostname: localhost
Port: 3307          ⚠️ QUAN TRỌNG: Dùng 3307, không phải 3306
Username: root
Password: quangtruong1
Default Schema: phongkhambenh
```

3. Click **"Test Connection"** để kiểm tra
4. Nếu thành công, click **"OK"** để lưu

### Bước 3: Kết Nối

- Double-click vào connection vừa tạo
- Nhập password nếu được yêu cầu: `quangtruong1`

---

## 📊 So Sánh 2 MySQL

Bây giờ bạn có **2 MySQL riêng biệt**:

| MySQL | Port | Dữ Liệu | Mục Đích |
|-------|------|---------|----------|
| **MySQL Local** | 3306 | Dữ liệu cũ | Dữ liệu từ trước |
| **MySQL Docker** | 3307 | Dữ liệu mới | App Docker đang dùng |

---

## 🔄 Migrate Dữ Liệu Từ Local Sang Docker (Nếu Cần)

Nếu bạn muốn copy dữ liệu từ MySQL Local (port 3306) sang MySQL Docker (port 3307):

### Cách 1: Dùng MySQL Workbench

1. **Kết nối MySQL Local** (port 3306)
2. **Export Database**:
   - Right-click database `phongkhambenh`
   - Chọn **"Data Export"**
   - Chọn tất cả tables
   - Export to: `backup_local.sql`
   - Click **"Start Export"**

3. **Kết nối MySQL Docker** (port 3307)
4. **Import Database**:
   - Right-click database `phongkhambenh` (hoặc tạo mới nếu chưa có)
   - Chọn **"Data Import/Restore"**
   - Chọn file `backup_local.sql`
   - Click **"Start Import"**

### Cách 2: Dùng Command Line

```bash
# 1. Backup từ MySQL local
mysqldump -u root -pquangtruong1 phongkhambenh > backup_local.sql

# 2. Import vào MySQL Docker (qua port 3307)
mysql -h localhost -P 3307 -u root -pquangtruong1 phongkhambenh < backup_local.sql
```

---

## ✅ Kiểm Tra Kết Nối

### Test Connection trong MySQL Workbench

Sau khi tạo connection, click **"Test Connection"**:
- ✅ Nếu thành công: "Successfully made the MySQL connection"
- ❌ Nếu lỗi: Kiểm tra lại port (phải là 3307) và password

### Kiểm Tra Bằng Command Line

```bash
# Test kết nối MySQL Docker (port 3307)
mysql -h localhost -P 3307 -u root -pquangtruong1 -e "SHOW DATABASES;"
```

---

## 🎯 Lưu Ý Quan Trọng

1. **Port 3307**: 
   - MySQL Docker đang expose qua port **3307** (không phải 3306)
   - Port 3306 vẫn là MySQL Local của bạn

2. **Dữ Liệu Khác Nhau**:
   - MySQL Local (3306) và MySQL Docker (3307) là **2 database riêng biệt**
   - Dữ liệu không tự động sync
   - App Docker đang dùng MySQL Docker (port 3307)

3. **Khi Test Bằng Postman**:
   - Dữ liệu sẽ được lưu vào **MySQL Docker** (port 3307)
   - Xem dữ liệu trong MySQL Workbench: kết nối port **3307**

---

## 🛠️ Troubleshooting

### Không kết nối được

1. **Kiểm tra containers đang chạy**:
   ```bash
   docker-compose ps
   ```

2. **Kiểm tra port đã expose**:
   ```bash
   docker-compose ps
   # Phải thấy: 0.0.0.0:3307->3306/tcp
   ```

3. **Kiểm tra logs MySQL**:
   ```bash
   docker-compose logs mysql
   ```

### Port 3307 đã được sử dụng

Nếu port 3307 đã được sử dụng, đổi sang port khác trong `docker-compose.yml`:
```yaml
ports:
  - "3308:3306"  # Đổi sang 3308
```

Sau đó restart:
```bash
docker-compose down
docker-compose up -d
```

---

## 📝 Tóm Tắt

✅ **Đã làm**:
- Expose MySQL Docker port 3307
- Containers đã được restart với cấu hình mới

✅ **Bạn cần làm**:
1. Mở MySQL Workbench
2. Tạo connection mới với port **3307**
3. (Tùy chọn) Migrate dữ liệu từ MySQL Local nếu cần

✅ **Kết quả**:
- Có thể dùng MySQL Workbench với MySQL Docker
- App Docker vẫn hoạt động bình thường
- Dữ liệu test từ Postman sẽ lưu vào MySQL Docker (port 3307)

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24


