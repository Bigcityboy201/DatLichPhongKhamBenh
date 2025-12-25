# Hướng Dẫn Sử Dụng Lại GitLab Runner

## ✅ Có Thể Sử Dụng Lại Runner

GitLab Runner có thể được **share giữa nhiều projects**. Bạn không cần đăng ký runner mới cho mỗi project.

---

## 🔍 Kiểm Tra Runner Hiện Tại

### 1. Xem Thông Tin Runner

Vào GitLab → **Settings → CI/CD → Runners** → Xem danh sách runners

Hoặc dùng command line:

```bash
# Xem danh sách runners đã đăng ký
gitlab-runner list
```

### 2. Kiểm Tra Tags và Cấu Hình

Trong GitLab UI, bạn sẽ thấy:

- **Runner Tags**: Ví dụ `local`, `docker`, `windows`
- **Runner Type**: Shared, Group, Project-specific
- **Executor**: shell, docker, docker-windows, etc.

---

## 🎯 Cách Sử Dụng Lại Runner

### Cách 1: Runner Đã Là Shared/Group Runner (Tự Động)

Nếu runner đã được cấu hình là **Shared Runner** hoặc **Group Runner**, nó sẽ tự động available cho tất cả projects trong group/instance.

**Không cần làm gì thêm** - chỉ cần đảm bảo tags trong `.gitlab-ci.yml` khớp với runner tags.

### Cách 2: Thêm Project Vào Runner (Project-Specific Runner)

Nếu runner là **Project-specific**, bạn cần thêm project mới vào runner:

#### Bước 1: Lấy Runner Token

1. Vào project mới → **Settings → CI/CD → Runners**
2. Copy **Registration token** (nếu là project-specific runner)

#### Bước 2: Thêm Project Vào Runner

```bash
# Xem cấu hình runner hiện tại
cat /etc/gitlab-runner/config.toml

# Hoặc trên Windows
type "C:\GitLab-Runner\config.toml"
```

#### Bước 3: Enable Runner Cho Project Mới

**Trong GitLab UI**:

1. Vào project mới → **Settings → CI/CD → Runners**
2. Tìm runner bạn muốn dùng
3. Click **"Enable for this project"**

**Hoặc dùng command line** (nếu có quyền):

```bash
# Thêm project vào runner (cần runner token của project mới)
gitlab-runner register \
  --url https://gitlab.com/ \
  --registration-token <PROJECT_TOKEN> \
  --executor shell \
  --tag-list "local"
```

---

## ⚙️ Kiểm Tra Tags Trong .gitlab-ci.yml

Đảm bảo tags trong file `.gitlab-ci.yml` khớp với runner tags:

```yaml
build_job:
  stage: build
  tags:
    - local # ← Phải khớp với runner tag
```

**Nếu runner có tag `local`**, thì job phải có `tags: - local`

**Nếu runner có tag `docker`**, thì job phải có `tags: - docker`

---

## 🔧 Các Trường Hợp Thường Gặp

### Trường Hợp 1: Runner Có Tag Khác

**Vấn đề**: Runner có tag `docker` nhưng job yêu cầu tag `local`

**Giải pháp**:

- **Option 1**: Sửa `.gitlab-ci.yml` để dùng tag của runner:

  ```yaml
  tags:
    - docker # Thay vì local
  ```

- **Option 2**: Thêm tag mới vào runner:
  ```bash
  # Sửa config.toml và thêm tag
  # Hoặc đăng ký lại runner với tag mới
  ```

### Trường Hợp 2: Runner Là Project-Specific

**Vấn đề**: Runner chỉ available cho 1 project

**Giải pháp**:

1. Vào project mới → **Settings → CI/CD → Runners**
2. Tìm runner cũ trong section "Available specific runners"
3. Click **"Enable for this project"**

### Trường Hợp 3: Runner Executor Khác

**Vấn đề**: Runner dùng executor `shell` nhưng job cần `docker`

**Giải pháp**:

- Nếu runner là `shell`: Đảm bảo Docker đã cài và có thể chạy `docker` command
- Nếu runner là `docker`: Đảm bảo có `docker:dind` service trong `.gitlab-ci.yml`

---

## 📋 Checklist Sử Dụng Lại Runner

- [ ] Xác định runner đã có (vào GitLab → Settings → CI/CD → Runners)
- [ ] Kiểm tra tags của runner
- [ ] Kiểm tra executor của runner (shell, docker, etc.)
- [ ] Sửa `.gitlab-ci.yml` để tags khớp với runner
- [ ] Enable runner cho project mới (nếu là project-specific)
- [ ] Test pipeline để đảm bảo runner hoạt động

---

## 🎯 Cấu Hình Đề Xuất

### Nếu Runner Dùng Shell Executor:

```yaml
build_job:
  stage: build
  tags:
    - local # Tag của runner
  script:
    - mvn clean package -DskipTests
```

### Nếu Runner Dùng Docker Executor:

```yaml
build_job:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  tags:
    - docker # Tag của runner
  script:
    - mvn clean package -DskipTests
```

---

## 🛠️ Troubleshooting

### Pipeline Không Chạy - "No runners available"

**Nguyên nhân**:

- Runner chưa được enable cho project
- Tags không khớp
- Runner không active

**Giải pháp**:

1. Vào project → **Settings → CI/CD → Runners**
2. Kiểm tra có runner nào available không
3. Enable runner nếu cần
4. Kiểm tra tags trong `.gitlab-ci.yml` khớp với runner tags

### Job Bị Stuck - "This job is stuck"

**Nguyên nhân**:

- Runner không chạy
- Runner không có tag phù hợp

**Giải pháp**:

```bash
# Kiểm tra runner status
gitlab-runner status

# Restart runner
gitlab-runner restart

# Xem logs
gitlab-runner --debug run
```

---

## 📝 Ví Dụ Cấu Hình

### Runner Đã Có:

- **Tags**: `local`, `windows`
- **Executor**: `shell`
- **OS**: Windows

### .gitlab-ci.yml Phù Hợp:

```yaml
build_job:
  stage: build
  tags:
    - local # Khớp với runner tag
  script:
    - mvn clean package -DskipTests
```

---

## ✅ Kết Luận

**Có thể sử dụng lại runner** cho nhiều projects. Chỉ cần:

1. ✅ Đảm bảo tags trong `.gitlab-ci.yml` khớp với runner tags
2. ✅ Enable runner cho project mới (nếu là project-specific)
3. ✅ Kiểm tra executor phù hợp (shell/docker)

**Không cần đăng ký runner mới** cho mỗi project!

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0
