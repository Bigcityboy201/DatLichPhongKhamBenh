# Hướng Dẫn CI/CD với GitLab cho Project DatLichPhongKham

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Cấu Hình GitLab Runner](#cấu-hình-gitlab-runner)
3. [Pipeline Stages](#pipeline-stages)
4. [Cấu Hình Pipeline](#cấu-hình-pipeline)
5. [Quy Trình Hoạt Động](#quy-trình-hoạt-động)
6. [Troubleshooting](#troubleshooting)

---

## 📖 Tổng Quan

Project sử dụng GitLab CI/CD để tự động hóa quá trình build, test và deploy ứng dụng Spring Boot. Pipeline sẽ tự động chạy khi có code được push lên repository.

### Kiến Trúc CI/CD

```
┌─────────────────────────────────────────────────────────┐
│              GitLab Repository                          │
│                                                         │
│  Push Code ────────► GitLab CI/CD Pipeline             │
│                          │                              │
│                          ▼                              │
│              ┌───────────────────────┐                  │
│              │  GitLab Runner        │                  │
│              │  (Local Machine)      │                  │
│              └───────────────────────┘                  │
│                          │                              │
│        ┌─────────────────┼─────────────────┐           │
│        ▼                 ▼                 ▼           │
│   ┌────────┐      ┌──────────┐      ┌──────────┐      │
│   │ Build  │ ───► │  Test    │ ───► │  Deploy  │      │
│   │ Stage  │      │  Stage   │      │  Stage   │      │
│   └────────┘      └──────────┘      └──────────┘      │
│        │                 │                 │           │
│        └─────────────────┴─────────────────┘           │
│                          │                              │
│                          ▼                              │
│              ┌───────────────────────┐                  │
│              │  Docker Containers    │                  │
│              │  (Running on Server)  │                  │
│              └───────────────────────┘                  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Cấu Hình GitLab Runner

### Yêu Cầu

- Máy tính đã cài đặt Docker và Docker Compose
- Quyền admin để cài đặt GitLab Runner
- Quyền truy cập vào GitLab project

### Bước 1: Cài Đặt GitLab Runner (Windows)

1. Tải GitLab Runner từ [GitLab Runner Releases](https://gitlab.com/gitlab-org/gitlab-runner/-/releases)

2. Tạo thư mục: `C:\GitLab-Runner`

3. Đặt file `gitlab-runner.exe` vào thư mục vừa tạo

4. Mở PowerShell với quyền Administrator và chạy:

```powershell
cd C:\GitLab-Runner
.\gitlab-runner.exe install
.\gitlab-runner.exe start
```

### Bước 2: Đăng Ký Runner

1. Vào GitLab project → **Settings** → **CI/CD** → **Runners**

2. Copy **Registration Token**

3. Chạy lệnh đăng ký:

```powershell
.\gitlab-runner.exe register
```

4. Trả lời các câu hỏi:

```
Enter the GitLab instance URL: https://gitlab.com/
Enter the registration token: [PASTE_TOKEN]
Enter a description: local-runner
Enter tags: local
Enter executor: shell
```

**Lưu ý:** Với Windows, chọn executor là `shell` (PowerShell)

### Bước 3: Cấu Hình Runner

File cấu hình: `C:\GitLab-Runner\config.toml`

```toml
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "local-runner"
  url = "https://gitlab.com/"
  token = "YOUR_TOKEN"
  executor = "shell"
  shell = "powershell"
  [runners.custom_build_dir]
```

### Bước 4: Khởi Động Runner

```powershell
.\gitlab-runner.exe start
```

Kiểm tra trạng thái:

```powershell
.\gitlab-runner.exe status
```

---

## 🏗️ Pipeline Stages

Pipeline được chia thành 3 stages chính:

### 1. Build Stage

**Mục đích:** Build ứng dụng Spring Boot thành JAR file

**Các bước:**
- Di chuyển vào thư mục `DatLichPhongKham`
- Chạy `mvn clean package -DskipTests`
- Lưu JAR file làm artifact

**Output:** File `target/*.jar`

### 2. Test Stage

**Mục đích:** Chạy unit tests

**Các bước:**
- Di chuyển vào thư mục `DatLichPhongKham`
- Chạy `mvn test`

**Output:** Test results

### 3. Deploy Stage

**Mục đích:** Deploy ứng dụng lên server bằng Docker

**Các bước:**
- Di chuyển vào thư mục `DatLichPhongKham`
- Dừng containers cũ: `docker-compose down`
- Build và khởi động containers mới: `docker-compose up -d --build`
- Kiểm tra containers đang chạy

**Điều kiện:** Chỉ chạy khi push vào branch `main`

---

## ⚙️ Cấu Hình Pipeline

File `.gitlab-ci.yml` nằm ở root của project:

```yaml
stages:
  - build
  - test
  - deploy

# ================= BUILD =================
build_job:
  stage: build
  tags:
    - local
  script:
    - echo "===== VAO THU MUC CHUA CODE ====="
    - cd DatLichPhongKham
    - echo "===== BAT DAU BUILD ====="
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - "DatLichPhongKham/target/*.jar"
    expire_in: 1 hour

# ================= TEST =================
test_job:
  stage: test
  tags:
    - local
  script:
    - cd DatLichPhongKham
    - echo "===== RUN TESTS ====="
    - mvn test

# ================= DEPLOY =================
deploy_job:
  stage: deploy
  tags:
    - local
  only:
    - main
  script:
    - cd DatLichPhongKham
    - echo "===== RESTARTING CONTAINERS ====="
    - docker-compose down
    - docker-compose up -d --build
    - Start-Sleep -Seconds 10
    - docker ps -a
```

### Giải Thích Các Thành Phần

#### Stages
```yaml
stages:
  - build    # Stage 1: Build application
  - test     # Stage 2: Run tests
  - deploy   # Stage 3: Deploy to server
```

#### Tags
```yaml
tags:
  - local    # Chỉ chạy trên runner có tag "local"
```

#### Artifacts
```yaml
artifacts:
  paths:
    - "DatLichPhongKham/target/*.jar"  # Lưu JAR file
  expire_in: 1 hour  # Tự động xóa sau 1 giờ
```

#### Only
```yaml
only:
  - main    # Chỉ chạy deploy job khi push vào branch main
```

---

## 🔄 Quy Trình Hoạt Động

### Quy Trình Khi Push Code

1. **Developer push code** lên GitLab repository

2. **GitLab CI/CD trigger** pipeline tự động

3. **Build Stage:**
   ```
   ✅ Checkout code
   ✅ cd DatLichPhongKham
   ✅ mvn clean package -DskipTests
   ✅ Save JAR artifact
   ```

4. **Test Stage:**
   ```
   ✅ cd DatLichPhongKham
   ✅ mvn test
   ✅ Report test results
   ```

5. **Deploy Stage** (chỉ khi push vào `main`):
   ```
   ✅ cd DatLichPhongKham
   ✅ docker-compose down
   ✅ docker-compose up -d --build
   ✅ Verify containers running
   ```

### Lưu Đồ Pipeline

```
┌─────────────┐
│  Push Code  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Build Stage    │
│  - Build JAR    │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  Test Stage     │
│  - Run Tests    │
└──────┬──────────┘
       │
       ▼
    ┌──────┐
    │ main?│
    └──┬───┘
       │ Yes
       ▼
┌─────────────────┐
│  Deploy Stage   │
│  - Docker Deploy│
└─────────────────┘
```

---

## 🐛 Troubleshooting

### Pipeline Không Chạy

**Nguyên nhân:**
- Runner chưa được đăng ký hoặc chưa active
- Tags không khớp
- Runner không online

**Giải pháp:**
```powershell
# Kiểm tra runner status
.\gitlab-runner.exe status

# Restart runner
.\gitlab-runner.exe restart

# Xem logs
.\gitlab-runner.exe --debug run
```

### Build Lỗi - Không Tìm Thấy pom.xml

**Nguyên nhân:**
- Thư mục code không đúng (cần vào `DatLichPhongKham/`)

**Giải pháp:**
```yaml
script:
  - cd DatLichPhongKham  # Đảm bảo có dòng này
  - mvn clean package
```

### Test Lỗi

**Nguyên nhân:**
- Tests fail
- Dependencies thiếu

**Giải pháp:**
```bash
# Chạy test local trước
cd DatLichPhongKham
mvn test

# Sửa lỗi test trước khi push
```

### Deploy Lỗi - Docker Không Chạy

**Nguyên nhân:**
- Docker daemon không chạy
- Port đã được sử dụng
- docker-compose.yml không đúng

**Giải pháp:**
```powershell
# Kiểm tra Docker
docker ps

# Kiểm tra docker-compose
cd DatLichPhongKham
docker-compose config

# Xem logs
docker-compose logs
```

### Container Không Restart

**Nguyên nhân:**
- Containers cũ chưa được dừng
- Port conflict

**Giải pháp:**
```powershell
# Force stop và remove
docker-compose down --remove-orphans

# Kiểm tra port
netstat -ano | findstr :8080

# Deploy lại
docker-compose up -d --build
```

### Runner Không Nhận Job

**Nguyên nhân:**
- Tags không khớp
- Runner không online
- Concurrent jobs limit

**Giải pháp:**
1. Kiểm tra tags trong `.gitlab-ci.yml` và `config.toml`
2. Đảm bảo runner online trong GitLab UI
3. Kiểm tra `concurrent` setting trong `config.toml`

---

## 📊 Monitoring và Logs

### Xem Pipeline Status

1. Vào GitLab project
2. **CI/CD** → **Pipelines**
3. Click vào pipeline để xem chi tiết

### Xem Job Logs

1. Click vào job trong pipeline
2. Xem logs real-time hoặc download logs

### Xem Runner Logs

```powershell
# Windows
Get-Content "C:\GitLab-Runner\logs\runner.log" -Tail 100

# Xem real-time
Get-Content "C:\GitLab-Runner\logs\runner.log" -Wait -Tail 50
```

---

## 🔒 Best Practices

### 1. Security

- **Không commit secrets** vào `.gitlab-ci.yml`
- Sử dụng **GitLab CI/CD Variables** cho sensitive data:
  - Vào **Settings** → **CI/CD** → **Variables**
  - Thêm variables như: `DB_PASSWORD`, `JWT_SECRET`, etc.

### 2. Performance

- **Cache Maven dependencies:**
  ```yaml
  cache:
    paths:
      - .m2/repository
  ```

- **Parallel jobs** nếu có nhiều runners
- **Artifact expiration** để tiết kiệm storage

### 3. Reliability

- **Health checks** sau khi deploy
- **Rollback strategy** nếu deploy fail
- **Backup database** trước khi deploy

### 4. Code Quality

- **Lint và format code** trước khi commit
- **Run tests local** trước khi push
- **Code review** trước khi merge vào main

---

## 🔧 Advanced Configuration

### Environment Variables trong GitLab

1. **Settings** → **CI/CD** → **Variables** → **Expand**
2. Thêm variables:
   - Key: `MYSQL_ROOT_PASSWORD`
   - Value: `your_password`
   - Protected: ✅ (chỉ dùng trong protected branches)
   - Masked: ✅ (ẩn trong logs)

### Conditional Deploy

```yaml
deploy_job:
  stage: deploy
  script:
    - docker-compose up -d --build
  only:
    - main
  when: on_success  # Chỉ deploy nếu các stage trước thành công
```

### Manual Deploy

```yaml
deploy_job:
  stage: deploy
  script:
    - docker-compose up -d --build
  when: manual  # Yêu cầu click nút để deploy
```

### Multi-Environment

```yaml
deploy_staging:
  stage: deploy
  script:
    - docker-compose -f docker-compose.staging.yml up -d --build
  only:
    - develop

deploy_production:
  stage: deploy
  script:
    - docker-compose -f docker-compose.prod.yml up -d --build
  only:
    - main
  when: manual
```

---

## 📝 Checklist Trước Khi Deploy

- [ ] ✅ Tests đều pass
- [ ] ✅ Code đã được review
- [ ] ✅ Environment variables đã được cấu hình
- [ ] ✅ Database backup đã được tạo
- [ ] ✅ Docker images đã được build thành công
- [ ] ✅ Health checks đang hoạt động
- [ ] ✅ Rollback plan đã sẵn sàng

---

## 🔗 Liên Kết

- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [GitLab Runner Documentation](https://docs.gitlab.com/runner/)
- [.gitlab-ci.yml Reference](https://docs.gitlab.com/ee/ci/yaml/)

