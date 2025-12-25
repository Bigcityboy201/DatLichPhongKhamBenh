# Hướng dẫn Setup Docker và Xử lý Lỗi

## Tổng quan

Tài liệu này mô tả quá trình setup Docker cho ứng dụng Spring Boot **DatLichPhongKham** và các lỗi đã gặp phải cũng như cách xử lý.

---

## 1. Các File Docker Đã Tạo

### 1.1. Dockerfile

**Mục đích**: Build image cho Spring Boot application

**Cấu trúc**:

- **Multi-stage build**:
  - Stage 1 (build): Sử dụng `maven:3.9-eclipse-temurin-17` để build ứng dụng
  - Stage 2 (runtime): Sử dụng `eclipse-temurin:17-jre-alpine` để chạy ứng dụng (nhẹ hơn)

**Các điểm đã xử lý**:

- ✅ Sử dụng multi-stage build để giảm kích thước image cuối cùng
- ✅ Tạo non-root user `spring` để chạy ứng dụng (bảo mật tốt hơn)
- ✅ Copy dependencies trước để tận dụng Docker cache
- ✅ Healthcheck đã được comment vì có thể chưa có actuator endpoint

**Nội dung chính**:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 1.2. docker-compose.yml

**Mục đích**: Cấu hình cho môi trường **Development**

**Services**:

1. **mysql**: MySQL 8.0 database
2. **app**: Spring Boot application

**Các vấn đề đã xử lý**:

#### ❌ Lỗi 1: Port 3306 Conflict

**Lỗi**:

```
Error response from daemon: ports are not available: exposing port TCP 0.0.0.0:3306 -> 127.0.0.1:0: listen tcp 0.0.0.0:3306: bind: Only one usage of each socket address (protocol/network address/port) is normally permitted.
```

**Nguyên nhân**:

- Máy đã có MySQL local chạy trên port 3306
- Docker cố gắng expose port 3306 ra ngoài gây conflict

**Giải pháp**:

- ✅ **Comment out phần ports** của MySQL service
- MySQL trong Docker chỉ accessible từ trong Docker network (qua service name `mysql`)
- App vẫn kết nối được vì cùng network
- Nếu cần truy cập từ host (MySQL Workbench), uncomment và đổi sang port khác (ví dụ: 3307)

```yaml
# Không expose port 3306 ra ngoài để tránh conflict với MySQL local
# Nếu cần truy cập MySQL từ host, uncomment dòng dưới và đổi port (ví dụ: 3307)
# ports:
#   - "${MYSQL_PORT:-3307}:3306"
```

#### ❌ Lỗi 2: MySQL User Configuration Error

**Lỗi**:

```
[ERROR] [Entrypoint]: MYSQL_USER="root", MYSQL_USER and MYSQL_PASSWORD are for configuring a regular user and cannot be used for the root user
```

**Nguyên nhân**:

- MySQL Docker image không cho phép tạo user `root` thông qua biến môi trường `MYSQL_USER`
- `MYSQL_USER` chỉ dùng để tạo user thường, không phải root

**Giải pháp**:

- ✅ **Xóa `MYSQL_USER` và `MYSQL_PASSWORD`** khi muốn dùng root
- ✅ Chỉ dùng `MYSQL_ROOT_PASSWORD` để set password cho root user
- ✅ App kết nối với username `root` và password từ `MYSQL_ROOT_PASSWORD`

**Trước** (SAI):

```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-quangtruong1}
  MYSQL_DATABASE: ${MYSQL_DATABASE:-phongkhambenh}
  MYSQL_USER: ${MYSQL_USER:-root} # ❌ Lỗi ở đây
  MYSQL_PASSWORD: ${MYSQL_PASSWORD:-quangtruong1}
```

**Sau** (ĐÚNG):

```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-quangtruong1}
  MYSQL_DATABASE: ${MYSQL_DATABASE:-phongkhambenh}
  # Không dùng MYSQL_USER="root" vì root là user đặc biệt
  # Nếu cần tạo user khác, uncomment và đổi tên user
  # MYSQL_USER: ${MYSQL_USER:-appuser}
  # MYSQL_PASSWORD: ${MYSQL_PASSWORD:-quangtruong1}
```

Và trong app service:

```yaml
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-quangtruong1}
```

#### ❌ Lỗi 3: Healthcheck Command Syntax Error

**Lỗi**:

```
java.lang.IllegalArgumentException: Invalid character found in the HTTP protocol [|| ]
```

**Nguyên nhân**:

- Healthcheck command có syntax sai: `"http://localhost:8080/api/health || exit 1"`
- Docker Compose không hỗ trợ `||` trong test command như shell script

**Giải pháp**:

- ✅ **Xóa phần `|| exit 1`** khỏi test command
- Docker tự động coi exit code != 0 là unhealthy

**Trước** (SAI):

```yaml
healthcheck:
  test:
    [
      "CMD",
      "wget",
      "--no-verbose",
      "--tries=1",
      "--spider",
      "http://localhost:8080/api/health || exit 1",
    ]
```

**Sau** (ĐÚNG):

```yaml
healthcheck:
  test:
    [
      "CMD",
      "wget",
      "--no-verbose",
      "--tries=1",
      "--spider",
      "http://localhost:8080/api/health",
    ]
```

#### ⚠️ Warning: Version Obsolete

**Warning**:

```
the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion
```

**Giải pháp**:

- ✅ **Xóa dòng `version: "3.8"`** vì Docker Compose v2+ không cần version nữa

---

### 1.3. docker-compose.prod.yml

**Mục đích**: Cấu hình cho môi trường **Production**

**Khác biệt so với development**:

- ✅ `restart: always` thay vì `unless-stopped`
- ✅ Không expose MySQL port ra ngoài (bảo mật)
- ✅ Tất cả environment variables phải được set (không có default values)
- ✅ Volume name khác: `mysql_data_prod` thay vì `mysql_data`
- ✅ Container names khác: `-prod` suffix

**Các lỗi đã xử lý**: Tương tự như `docker-compose.yml`

---

### 1.4. .dockerignore

**Mục đích**: Loại trừ các file không cần thiết khi build image

**Các file/folder đã loại trừ**:

- `target/` - Build artifacts
- `.idea/`, `.vscode/` - IDE configs
- `*.iml`, `*.ipr`, `*.iws` - IntelliJ files
- `.git/` - Git repository
- `*.md` - Documentation files
- `src/test/` - Test files (không cần trong production)
- `.env*` - Environment files
- `Dockerfile`, `docker-compose.yml` - Docker configs

**Lợi ích**:

- ✅ Giảm build context size
- ✅ Tăng tốc độ build
- ✅ Bảo mật (không copy sensitive files)

---

### 1.5. application.properties Updates

**Mục đích**: Hỗ trợ environment variables từ Docker

**Các thay đổi**:

- ✅ Thêm support cho `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- ✅ Thêm support cho `SPRING_JPA_HIBERNATE_DDL_AUTO`, `SPRING_JPA_SHOW_SQL`
- ✅ Thêm support cho `JWT_SECRET`, `JWT_DURATION`

**Ví dụ**:

```properties
# Trước
spring.datasource.url=jdbc:mysql://localhost:3306/phongkhambenh?createDatabaseIfNotExist=true

# Sau
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/phongkhambenh?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh}
```

**Lưu ý**:

- Trong Docker, database URL phải dùng hostname `mysql` (service name) thay vì `localhost`
- Thêm các tham số: `useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh`

---

### 1.6. env.example

**Mục đích**: Template cho environment variables

**Nội dung**: Chứa tất cả các biến môi trường cần thiết với giá trị mặc định

**Cách sử dụng**:

```bash
cp env.example .env
# Chỉnh sửa .env với các giá trị thực tế
```

---

## 2. Quy Trình Setup

### 2.1. Development Environment

```bash
# 1. Copy file env (nếu cần)
cp env.example .env

# 2. Build và chạy containers
docker-compose up -d

# 3. Xem logs
docker-compose logs -f app

# 4. Kiểm tra status
docker-compose ps

# 5. Dừng containers
docker-compose down

# 6. Dừng và xóa volumes (xóa database)
docker-compose down -v
```

### 2.2. Production Environment

```bash
# 1. Tạo file .env với giá trị production
cp env.example .env
# Chỉnh sửa .env với các giá trị production

# 2. Build và chạy
docker-compose -f docker-compose.prod.yml up -d

# 3. Xem logs
docker-compose -f docker-compose.prod.yml logs -f app

# 4. Dừng
docker-compose -f docker-compose.prod.yml down
```

---

## 3. Các Lệnh Hữu Ích

### 3.1. Quản lý Containers

```bash
# Xem status
docker-compose ps

# Xem logs
docker-compose logs -f app
docker-compose logs -f mysql

# Restart service
docker-compose restart app

# Rebuild image
docker-compose build --no-cache app
docker-compose up -d
```

### 3.2. Database Operations

```bash
# Truy cập MySQL container
docker-compose exec mysql mysql -u root -p

# Backup database
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < backup.sql
```

### 3.3. Debugging

```bash
# Xem resource usage
docker stats

# Xem chi tiết container
docker inspect datlichphongkham-app

# Vào trong container
docker-compose exec app sh
```

---

## 4. Cấu Trúc Network

```
┌─────────────────────────────────────────┐
│         Docker Network (bridge)          │
│                                          │
│  ┌──────────────┐    ┌──────────────┐   │
│  │   MySQL      │    │  Spring Boot │   │
│  │  Container   │◄───┤   Container  │   │
│  │              │    │              │   │
│  │  Port: 3306  │    │  Port: 8080  │   │
│  │  (internal)  │    │  (exposed)   │   │
│  └──────────────┘    └──────────────┘   │
│                                          │
└─────────────────────────────────────────┘
         ▲
         │
         │ (không expose)
         │
    Host Machine
    (localhost:8080)
```

**Giải thích**:

- MySQL chỉ accessible từ trong Docker network
- App expose port 8080 ra ngoài để truy cập từ host
- App kết nối MySQL qua service name `mysql:3306`

---

## 5. Health Checks

### 5.1. MySQL Health Check

```yaml
healthcheck:
  test:
    [
      "CMD",
      "mysqladmin",
      "ping",
      "-h",
      "localhost",
      "-u",
      "root",
      "-p${MYSQL_ROOT_PASSWORD:-quangtruong1}",
    ]
  interval: 10s
  timeout: 5s
  retries: 5
```

**Mục đích**: Đảm bảo MySQL đã sẵn sàng trước khi app start

### 5.2. App Health Check

```yaml
healthcheck:
  test:
    [
      "CMD",
      "wget",
      "--no-verbose",
      "--tries=1",
      "--spider",
      "http://localhost:8080/api/health",
    ]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

**Lưu ý**:

- Endpoint `/api/health` cần được implement trong ứng dụng
- Nếu chưa có, có thể tạm thời comment healthcheck này

---

## 6. Volumes và Data Persistence

### 6.1. MySQL Data Volume

```yaml
volumes:
  mysql_data:
    driver: local
```

**Mục đích**:

- Lưu trữ database data
- Data không bị mất khi container bị xóa
- Chỉ mất khi chạy `docker-compose down -v`

### 6.2. Init Scripts

```yaml
volumes:
  - ./mysql-init:/docker-entrypoint-initdb.d
```

**Mục đích**:

- Chạy các SQL scripts khi database được khởi tạo lần đầu
- Có thể tạo folder `mysql-init/` và thêm các file `.sql` vào đó

---

## 7. Environment Variables

### 7.1. Database Configuration

```bash
MYSQL_ROOT_PASSWORD=quangtruong1
MYSQL_DATABASE=phongkhambenh
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/phongkhambenh?...
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=quangtruong1
```

### 7.2. Application Configuration

```bash
APP_PORT=8080
JWT_SECRET=...
JWT_DURATION=604800
ADMIN_USERNAME=quangtruongngo2012004
ADMIN_PASSWORD=quangtruong1
ADMIN_EMAIL=quangtruong2012004@gmail.com
```

### 7.3. Payment Configuration

```bash
MOMO_PARTNER_CODE=...
MOMO_ACCESS_KEY=...
MOMO_SECRET_KEY=...
QRCODE_BANK_ID=970422
QRCODE_BANK_ACCOUNT=0363159912
QRCODE_BANK_NAME=BIDV
CASSO_WEBHOOK_SECRET_KEY=...
```

---

## 8. Troubleshooting

### 8.1. App không kết nối được database

**Kiểm tra**:

1. MySQL container đã healthy chưa: `docker-compose ps`
2. Database URL có dùng hostname `mysql` không (không phải `localhost`)
3. Username/password có đúng không
4. Xem logs: `docker-compose logs mysql`

### 8.2. Port đã được sử dụng

**Giải pháp**:

- Đổi `APP_PORT` trong `.env` hoặc `docker-compose.yml`
- Hoặc dừng service đang dùng port đó

### 8.3. Build image bị lỗi

**Giải pháp**:

```bash
docker-compose build --no-cache
docker-compose up -d
```

### 8.4. Container không start

**Kiểm tra**:

```bash
docker-compose logs app
docker-compose logs mysql
docker-compose ps
```

---

## 9. Best Practices

### 9.1. Security

- ✅ Không commit file `.env` vào Git
- ✅ Đổi tất cả default passwords trong production
- ✅ Sử dụng non-root user trong container
- ✅ Không expose MySQL port ra ngoài trong production

### 9.2. Performance

- ✅ Sử dụng multi-stage build để giảm image size
- ✅ Tận dụng Docker cache (copy pom.xml trước)
- ✅ Sử dụng `.dockerignore` để giảm build context

### 9.3. Maintainability

- ✅ Tách riêng `docker-compose.yml` (dev) và `docker-compose.prod.yml` (prod)
- ✅ Sử dụng environment variables thay vì hardcode
- ✅ Document rõ ràng các cấu hình

---

## 10. Tóm Tắt Các Lỗi Đã Xử Lý

| #   | Lỗi                      | Nguyên nhân                          | Giải pháp                                    |
| --- | ------------------------ | ------------------------------------ | -------------------------------------------- |
| 1   | Port 3306 conflict       | MySQL local đã chạy trên port 3306   | Comment out ports của MySQL service          |
| 2   | MySQL user error         | Không thể dùng MYSQL_USER="root"     | Xóa MYSQL_USER, chỉ dùng MYSQL_ROOT_PASSWORD |
| 3   | Healthcheck syntax error | Command có `\|\| exit 1`             | Xóa phần `\|\| exit 1`                       |
| 4   | Version obsolete warning | Docker Compose v2+ không cần version | Xóa dòng `version: "3.8"`                    |

---

## 11. Kết Luận

Sau khi xử lý tất cả các lỗi trên, hệ thống Docker đã hoạt động ổn định:

- ✅ MySQL container: Healthy
- ✅ App container: Running trên port 8080
- ✅ Database: Đã kết nối và tạo tables thành công
- ✅ Application: Đã start và tạo admin user thành công

**Lưu ý quan trọng**:

- Trong production, cần set tất cả environment variables
- Đổi tất cả default passwords
- Cân nhắc sử dụng reverse proxy (nginx) và SSL certificate
- Backup database thường xuyên

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0

