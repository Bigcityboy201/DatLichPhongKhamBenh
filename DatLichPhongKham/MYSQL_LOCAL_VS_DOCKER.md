# Hướng Dẫn: MySQL Local vs MySQL Docker

## ⚠️ Tình Huống Hiện Tại

Bạn đang có **2 MySQL riêng biệt**:

1. **MySQL Local** (đã có từ trước)

   - Chạy trên máy của bạn
   - Port: `3306`
   - Database: `phongkhambenh` (có dữ liệu cũ)
   - Truy cập: `localhost:3306`

2. **MySQL Docker** (mới tạo)
   - Chạy trong Docker container
   - Port: `3306` (trong container, KHÔNG expose ra ngoài)
   - Database: `phongkhambenh` (mới, chưa có dữ liệu)
   - Truy cập: Chỉ từ trong Docker network

---

## 🔍 Phân Tích

### Hiện Tại App Đang Dùng MySQL Nào?

Khi bạn chạy `docker-compose up -d`, app trong Docker đang kết nối với **MySQL Docker** (KHÔNG phải MySQL local).

**Lý do**:

- App container kết nối qua hostname `mysql` (service name trong Docker)
- URL: `jdbc:mysql://mysql:3306/phongkhambenh`
- Đây là MySQL trong Docker, không phải MySQL local

### Vấn Đề

- ✅ **MySQL Local**: Có dữ liệu cũ nhưng app Docker không dùng
- ✅ **MySQL Docker**: App đang dùng nhưng chưa có dữ liệu cũ

---

## 🎯 Giải Pháp

Bạn có **3 lựa chọn**:

### **Lựa Chọn 1: Dùng MySQL Local (Không Dùng Docker MySQL)** ⭐ Khuyến nghị nếu muốn giữ dữ liệu cũ

**Ưu điểm**:

- Giữ nguyên dữ liệu cũ
- Không cần migrate
- Dùng MySQL Workbench như bình thường

**Cách làm**:

1. **Dừng MySQL container** (hoặc xóa service MySQL khỏi docker-compose.yml)

2. **Sửa docker-compose.yml**:

```yaml
services:
  # Xóa hoặc comment service mysql
  # mysql:
  #   ...

  app:
    # ...
    environment:
      # Đổi URL để kết nối MySQL local
      SPRING_DATASOURCE_URL: jdbc:mysql://host.docker.internal:3306/phongkhambenh?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: quangtruong1
```

3. **Restart app**:

```bash
docker-compose up -d app
```

**Lưu ý**:

- `host.docker.internal` là hostname đặc biệt để container kết nối về máy host
- Đảm bảo MySQL local đang chạy
- MySQL Workbench kết nối bình thường: `localhost:3306`

---

### **Lựa Chọn 2: Dùng MySQL Docker + Migrate Dữ Liệu** ⭐ Khuyến nghị nếu muốn dùng Docker hoàn toàn

**Ưu điểm**:

- Tách biệt hoàn toàn với MySQL local
- Dễ deploy, backup, restore
- Có thể dùng MySQL Workbench (cần expose port)

**Cách làm**:

#### Bước 1: Expose MySQL Docker Port

Sửa `docker-compose.yml`:

```yaml
mysql:
  ports:
    - "3307:3306" # Expose port 3307 ra ngoài (tránh conflict với MySQL local 3306)
```

#### Bước 2: Restart Docker

```bash
docker-compose down
docker-compose up -d
```

#### Bước 3: Backup Dữ Liệu Từ MySQL Local

```bash
# Backup database từ MySQL local
mysqldump -u root -pquangtruong1 phongkhambenh > backup_from_local.sql
```

#### Bước 4: Import Vào MySQL Docker

```bash
# Import vào MySQL Docker (qua port 3307)
mysql -h localhost -P 3307 -u root -pquangtruong1 phongkhambenh < backup_from_local.sql
```

Hoặc dùng MySQL Workbench:

- **Host**: `localhost`
- **Port**: `3307`
- **Username**: `root`
- **Password**: `quangtruong1`
- Import file `backup_from_local.sql`

#### Bước 5: Kết Nối MySQL Workbench Với Docker

**Cấu hình kết nối mới**:

- **Connection Name**: `Docker MySQL`
- **Hostname**: `localhost`
- **Port**: `3307` ⚠️ (không phải 3306)
- **Username**: `root`
- **Password**: `quangtruong1`
- **Default Schema**: `phongkhambenh`

---

### **Lựa Chọn 3: Dùng Cả Hai (Development vs Production)**

**Cách làm**:

- **Development**: Dùng MySQL local (giữ dữ liệu cũ)
- **Production**: Dùng MySQL Docker (sạch sẽ, dễ deploy)

**Cấu hình**:

1. **docker-compose.yml** (Development - dùng MySQL local):

```yaml
services:
  app:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://host.docker.internal:3306/phongkhambenh?...
```

2. **docker-compose.prod.yml** (Production - dùng MySQL Docker):

```yaml
services:
  mysql:
    # ... MySQL trong Docker

  app:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/phongkhambenh?...
```

---

## 📋 Hướng Dẫn Chi Tiết: Kết Nối MySQL Workbench Với Docker

### Bước 1: Expose Port MySQL Docker

Sửa `docker-compose.yml`:

```yaml
mysql:
  ports:
    - "3307:3306" # Map port 3307 (host) -> 3306 (container)
```

### Bước 2: Restart Container

```bash
docker-compose down
docker-compose up -d
```

### Bước 3: Tạo Connection Trong MySQL Workbench

1. Mở MySQL Workbench
2. Click **"+"** để tạo connection mới
3. Điền thông tin:
   ```
   Connection Name: Docker MySQL - PhongKhamBenh
   Hostname: localhost
   Port: 3307          ⚠️ QUAN TRỌNG: Dùng 3307, không phải 3306
   Username: root
   Password: quangtruong1
   Default Schema: phongkhambenh
   ```
4. Click **"Test Connection"** để kiểm tra
5. Click **"OK"** để lưu

### Bước 4: Sử Dụng

- **MySQL Local** (port 3306): Dữ liệu cũ
- **MySQL Docker** (port 3307): Dữ liệu mới từ app Docker

---

## 🔄 Migrate Dữ Liệu Từ Local Sang Docker

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
   - Right-click database `phongkhambenh` (hoặc tạo mới)
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

### Cách 3: Dùng Docker Exec

```bash
# 1. Backup từ MySQL local
mysqldump -u root -pquangtruong1 phongkhambenh > backup_local.sql

# 2. Copy file vào container
docker cp backup_local.sql datlichphongkham-mysql:/tmp/backup_local.sql

# 3. Import trong container
docker-compose exec mysql mysql -u root -pquangtruong1 phongkhambenh < backup_local.sql
```

---

## 🎯 Khuyến Nghị

### Nếu Bạn Muốn:

#### ✅ **Giữ Dữ Liệu Cũ + Dùng MySQL Workbench Dễ Dàng**

→ **Chọn Lựa Chọn 1**: Dùng MySQL Local

- App Docker kết nối MySQL local qua `host.docker.internal:3306`
- MySQL Workbench kết nối bình thường `localhost:3306`
- Không cần migrate

#### ✅ **Dùng Docker Hoàn Toàn + Có Thể Dùng MySQL Workbench**

→ **Chọn Lựa Chọn 2**: Dùng MySQL Docker + Migrate

- Expose port 3307
- Migrate dữ liệu từ local sang Docker
- MySQL Workbench kết nối `localhost:3307`

#### ✅ **Tách Biệt Development và Production**

→ **Chọn Lựa Chọn 3**: Dùng cả hai

- Dev: MySQL local
- Prod: MySQL Docker

---

## 📊 So Sánh

| Tiêu chí            | MySQL Local             | MySQL Docker                  |
| ------------------- | ----------------------- | ----------------------------- |
| **Dữ liệu cũ**      | ✅ Có sẵn               | ❌ Cần migrate                |
| **MySQL Workbench** | ✅ Dễ (port 3306)       | ⚠️ Cần expose port 3307       |
| **Tách biệt**       | ❌ Dùng chung với local | ✅ Hoàn toàn tách biệt        |
| **Deploy**          | ❌ Phức tạp             | ✅ Dễ (có sẵn trong Docker)   |
| **Backup**          | ⚠️ Phải backup riêng    | ✅ Backup cùng với app        |
| **Port conflict**   | ⚠️ Có thể conflict      | ✅ Không conflict (port khác) |

---

## 🛠️ Quick Commands

### Kiểm Tra MySQL Local

```bash
# Kết nối MySQL local
mysql -u root -pquangtruong1

# Xem databases
SHOW DATABASES;

# Xem tables trong phongkhambenh
USE phongkhambenh;
SHOW TABLES;
```

### Kiểm Tra MySQL Docker

```bash
# Kết nối MySQL Docker (nếu đã expose port 3307)
mysql -h localhost -P 3307 -u root -pquangtruong1

# Hoặc vào container
docker-compose exec mysql mysql -u root -pquangtruong1
```

### So Sánh Dữ Liệu

```bash
# Đếm records trong MySQL local
mysql -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) FROM users;"

# Đếm records trong MySQL Docker (port 3307)
mysql -h localhost -P 3307 -u root -pquangtruong1 phongkhambenh -e "SELECT COUNT(*) FROM users;"
```

---

## ⚠️ Lưu Ý Quan Trọng

1. **Port Conflict**:

   - MySQL local: port 3306
   - MySQL Docker: nên expose port 3307 (không phải 3306)

2. **Dữ Liệu Khác Nhau**:

   - MySQL local và MySQL Docker là **2 database riêng biệt**
   - Dữ liệu không tự động sync
   - Cần migrate nếu muốn dùng dữ liệu cũ

3. **App Đang Dùng MySQL Nào**:

   - Kiểm tra `SPRING_DATASOURCE_URL` trong docker-compose.yml
   - `mysql:3306` = MySQL Docker
   - `host.docker.internal:3306` = MySQL Local

4. **Backup**:
   - Luôn backup trước khi migrate hoặc xóa dữ liệu

---

## 📝 Tóm Tắt

**Câu hỏi**: "Tôi lưu trong MySQL Workbench khi dùng Docker hay sao?"

**Trả lời**:

- ✅ **Có thể dùng MySQL Workbench** với cả MySQL local và MySQL Docker
- ✅ **MySQL Local** (port 3306): Kết nối bình thường
- ✅ **MySQL Docker** (port 3307): Cần expose port và kết nối qua port 3307
- ⚠️ **Quan trọng**: App Docker hiện đang dùng MySQL Docker (không phải MySQL local)
- 💡 **Khuyến nghị**:
  - Nếu muốn giữ dữ liệu cũ → Dùng MySQL Local
  - Nếu muốn dùng Docker hoàn toàn → Migrate dữ liệu sang MySQL Docker

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0

