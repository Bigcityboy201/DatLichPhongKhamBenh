# Hướng dẫn Deploy với Docker

## Yêu cầu
- Docker Engine 20.10+
- Docker Compose 2.0+

## Cấu trúc Files
- `Dockerfile`: Build image cho Spring Boot application
- `docker-compose.yml`: Cấu hình cho môi trường development
- `docker-compose.prod.yml`: Cấu hình cho môi trường production
- `.dockerignore`: Loại trừ các file không cần thiết khi build
- `.env.example`: Template cho file environment variables

## Quick Start

### 1. Development Environment

```bash
# Copy file .env.example thành .env và chỉnh sửa nếu cần
cp .env.example .env

# Build và chạy containers
docker-compose up -d

# Xem logs
docker-compose logs -f app

# Dừng containers
docker-compose down

# Dừng và xóa volumes (xóa database)
docker-compose down -v
```

### 2. Production Environment

```bash
# Tạo file .env với các giá trị production
cp .env.example .env
# Chỉnh sửa .env với các giá trị production (đặc biệt là passwords và secrets)

# Build và chạy với production config
docker-compose -f docker-compose.prod.yml up -d

# Xem logs
docker-compose -f docker-compose.prod.yml logs -f app

# Dừng containers
docker-compose -f docker-compose.prod.yml down
```

## Cấu hình Environment Variables

Tạo file `.env` từ `.env.example` và cập nhật các giá trị:

### Database
- `MYSQL_ROOT_PASSWORD`: Password cho MySQL root user
- `MYSQL_DATABASE`: Tên database
- `MYSQL_USER`: MySQL user
- `MYSQL_PASSWORD`: MySQL password

### Application
- `APP_PORT`: Port cho Spring Boot app (mặc định: 8080)
- `JWT_SECRET`: Secret key cho JWT (nên dùng giá trị mạnh trong production)
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_EMAIL`: Thông tin admin user

### Payment Configuration
- `MOMO_PARTNER_CODE`, `MOMO_ACCESS_KEY`, `MOMO_SECRET_KEY`: Thông tin MoMo
- `QRCODE_BANK_ID`: Bank ID (970422 cho MB, 970418 cho BIDV)
- `QRCODE_BANK_ACCOUNT`: Số tài khoản ngân hàng
- `QRCODE_BANK_NAME`: Tên ngân hàng (BIDV, MB Bank, etc.)
- `CASSO_WEBHOOK_SECRET_KEY`: Secret key cho Casso webhook

## Các lệnh hữu ích

```bash
# Rebuild image sau khi thay đổi code
docker-compose build --no-cache app
docker-compose up -d

# Xem logs của MySQL
docker-compose logs -f mysql

# Truy cập MySQL container
docker-compose exec mysql mysql -u root -p

# Restart service
docker-compose restart app

# Xem status
docker-compose ps

# Xem resource usage
docker stats
```

## Troubleshooting

### App không kết nối được database
- Kiểm tra MySQL đã healthy chưa: `docker-compose ps`
- Kiểm tra logs: `docker-compose logs mysql`
- Đảm bảo `SPRING_DATASOURCE_URL` sử dụng hostname `mysql` (không phải `localhost`)

### Port đã được sử dụng
- Thay đổi `APP_PORT` trong `.env` hoặc `docker-compose.yml`
- Hoặc dừng service đang dùng port đó

### Build image bị lỗi
- Xóa cache: `docker-compose build --no-cache`
- Kiểm tra Dockerfile có đúng syntax không

## Backup Database

```bash
# Backup
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backup.sql

# Restore
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < backup.sql
```

## Health Checks

- MySQL: Kiểm tra bằng `mysqladmin ping`
- App: Kiểm tra endpoint `/actuator/health` (cần thêm Spring Boot Actuator dependency nếu chưa có)

## Notes

- Database data được lưu trong Docker volume `mysql_data` (development) hoặc `mysql_data_prod` (production)
- Trong production, nên sử dụng reverse proxy (nginx) và SSL certificate
- Đảm bảo thay đổi tất cả default passwords trong production


