# Hướng Dẫn Sử Dụng Docker cho Project DatLichPhongKham

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Cấu Trúc Docker](#cấu-trúc-docker)
3. [Cài Đặt và Khởi Chạy](#cài-đặt-và-khởi-chạy)
4. [Cấu Hình](#cấu-hình)
5. [Quản Lý Container](#quản-lý-container)
6. [Troubleshooting](#troubleshooting)

---

## 📖 Tổng Quan

Project sử dụng Docker và Docker Compose để containerize ứng dụng Spring Boot cùng với MySQL database. Hệ thống bao gồm:

- **Spring Boot Application** (port 8080)
- **MySQL Database** (port 3307 - tránh conflict với MySQL local)

### Kiến Trúc Docker

```
┌─────────────────────────────────────────┐
│         Docker Network                  │
│         (app-network)                   │
│                                         │
│  ┌─────────────┐      ┌──────────────┐ │
│  │   MySQL     │◄─────┤ Spring Boot  │ │
│  │  Container  │      │  Container   │ │
│  │  Port 3307  │      │  Port 8080   │ │
│  └─────────────┘      └──────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🏗️ Cấu Trúc Docker

### Các File Quan Trọng

1. **Dockerfile**

   - Multi-stage build để tối ưu kích thước image
   - Stage 1: Build với Maven (maven:3.9-eclipse-temurin-17)
   - Stage 2: Runtime với JRE (eclipse-temurin:17-jre-alpine)
   - Tạo non-root user để tăng bảo mật

2. **docker-compose.yml** (Development)

   - Cấu hình cho môi trường phát triển
   - Expose MySQL port 3307 ra ngoài để dùng MySQL Workbench

3. **docker-compose.prod.yml** (Production)

   - Cấu hình cho môi trường sản xuất
   - `restart: always` để tự động khởi động lại
   - Không expose MySQL port

4. **.dockerignore**

   - Loại bỏ các file không cần thiết khỏi Docker build context

5. **env.example**
   - Template cho các biến môi trường

---

## 🚀 Cài Đặt và Khởi Chạy

### Yêu Cầu Hệ Thống

- Docker >= 20.10
- Docker Compose >= 2.0

### Bước 1: Tạo File Environment

```bash
cp env.example .env
```

Sau đó chỉnh sửa file `.env` với các giá trị phù hợp.

### Bước 2: Khởi Chạy Development Environment

```bash
# Build và start containers
docker-compose up -d --build

# Xem logs
docker-compose logs -f

# Xem status
docker-compose ps
```

### Bước 3: Khởi Chạy Production Environment

```bash
# Sử dụng file compose riêng cho production
docker-compose -f docker-compose.prod.yml up -d --build
```

### Bước 4: Kiểm Tra Health

```bash
# Kiểm tra MySQL health
docker-compose exec mysql mysqladmin ping -h localhost -u root -p

# Kiểm tra App health
curl http://localhost:8080/api/health
```

---

## ⚙️ Cấu Hình

### Environment Variables

Các biến môi trường quan trọng trong `.env`:

#### Database Configuration

```env
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=phongkhambenh
MYSQL_PORT=3307
```

#### Application Configuration

```env
APP_PORT=8080
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

#### JWT Configuration

```env
JWT_SECRET=your_secret_key
JWT_DURATION=604800
```

#### Admin Configuration

```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin_password
ADMIN_EMAIL=admin@example.com
```

#### Payment Configuration

```env
QRCODE_BANK_ID=970422
QRCODE_BANK_ACCOUNT=your_account
DEPOSIT_AMOUNT=2000
CASSO_WEBHOOK_SECRET_KEY=your_secret_key
```

### Dockerfile Multi-Stage Build

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Lợi ích của multi-stage build:**

- Giảm kích thước image cuối cùng (chỉ chứa JRE, không có Maven)
- Tăng bảo mật (không có build tools trong production image)
- Tăng tốc độ build (layer caching)

---

## 🔧 Quản Lý Container

### Các Lệnh Cơ Bản

```bash
# Start containers
docker-compose up -d

# Stop containers
docker-compose down

# Stop và xóa volumes (⚠️ Xóa dữ liệu)
docker-compose down -v

# Restart containers
docker-compose restart

# Xem logs
docker-compose logs -f app
docker-compose logs -f mysql

# Xem logs của 1 container cụ thể
docker logs datlichphongkham-app
docker logs datlichphongkham-mysql

# Vào shell của container
docker-compose exec app sh
docker-compose exec mysql bash

# Rebuild khi có thay đổi code
docker-compose up -d --build

# Xem resource usage
docker stats
```

### Quản Lý Database

```bash
# Kết nối MySQL từ host
mysql -h 127.0.0.1 -P 3307 -u root -p

# Backup database
docker-compose exec mysql mysqldump -u root -p phongkhambenh > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p phongkhambenh < backup.sql

# Xem database volumes
docker volume ls
docker volume inspect datlichphongkham_mysql_data
```

### MySQL Workbench Connection

Để kết nối MySQL Workbench với Docker MySQL:

1. Tạo connection mới
2. **Hostname:** `127.0.0.1`
3. **Port:** `3307`
4. **Username:** `root`
5. **Password:** Giá trị từ `MYSQL_ROOT_PASSWORD` trong `.env`

---

## 🐛 Troubleshooting

### Container Không Khởi Động

```bash
# Kiểm tra logs
docker-compose logs app
docker-compose logs mysql

# Kiểm tra status
docker-compose ps

# Kiểm tra health check
docker inspect datlichphongkham-app | grep -A 10 Health
docker inspect datlichphongkham-mysql | grep -A 10 Health
```

### Port Đã Được Sử Dụng

Nếu port 8080 hoặc 3307 đã được sử dụng:

```bash
# Kiểm tra process đang dùng port
netstat -ano | findstr :8080
netstat -ano | findstr :3307

# Hoặc thay đổi port trong .env
APP_PORT=8081
MYSQL_PORT=3308
```

### MySQL Connection Error

```bash
# Kiểm tra MySQL container có chạy không
docker-compose ps mysql

# Kiểm tra MySQL logs
docker-compose logs mysql

# Test connection
docker-compose exec mysql mysqladmin ping -h localhost -u root -p

# Reset MySQL nếu cần (⚠️ Mất dữ liệu)
docker-compose down -v
docker-compose up -d mysql
```

### App Không Kết Nối Được MySQL

1. Kiểm tra `depends_on` trong docker-compose.yml
2. Kiểm tra `SPRING_DATASOURCE_URL` trong environment variables
3. Đảm bảo MySQL container healthy trước khi app start:
   ```yaml
   depends_on:
     mysql:
       condition: service_healthy
   ```

### Build Lỗi

```bash
# Clean và rebuild
docker-compose down
docker system prune -f
docker-compose build --no-cache
docker-compose up -d
```

### Xóa Dữ Liệu và Bắt Đầu Lại

```bash
# Stop và xóa tất cả (bao gồm volumes)
docker-compose down -v

# Xóa images
docker rmi datlichphongkham-app

# Bắt đầu lại
docker-compose up -d --build
```

---

## 📊 Data Persistence

### Volumes

Docker volumes được sử dụng để lưu trữ dữ liệu MySQL:

```yaml
volumes:
  mysql_data:
    driver: local
```

**Vị trí lưu trữ:**

- Windows: `\\wsl$\docker-desktop-data\data\docker\volumes\datlichphongkham_mysql_data`
- Linux/Mac: `/var/lib/docker/volumes/datlichphongkham_mysql_data`

### Backup và Restore

```bash
# Backup volume
docker run --rm -v datlichphongkham_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# Restore volume
docker run --rm -v datlichphongkham_mysql_data:/data -v $(pwd):/backup alpine sh -c "cd /data && tar xzf /backup/mysql-backup.tar.gz --strip 1"
```

---

## 🔒 Bảo Mật

1. **Non-root User**: App container chạy với user `spring` thay vì root
2. **Environment Variables**: Không commit file `.env` vào Git
3. **Network Isolation**: Services chỉ giao tiếp qua Docker network
4. **Health Checks**: Đảm bảo services healthy trước khi ready

---

## 📝 Best Practices

1. **Sử dụng .env file** cho configuration
2. **Không commit .env** vào Git (đã có trong .gitignore)
3. **Backup database** thường xuyên
4. **Monitor logs** để phát hiện lỗi sớm
5. **Sử dụng health checks** để đảm bảo services ready
6. **Clean up** unused images và containers định kỳ:
   ```bash
   docker system prune -a
   ```

---

## 🔗 Liên Kết

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
