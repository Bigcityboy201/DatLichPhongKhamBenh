# Quick Fix: Pipeline Không Chạy

## ✅ Thông Tin Hiện Tại

- ✅ File `.gitlab-ci.yml` đã tồn tại
- ✅ File đã được commit (commit: e730ef2)
- ✅ Remote GitLab: `gitlab` → `git@gitlab.com:ngoquangtruong2012004-group/datlichphongkham.git`
- ✅ Branch hiện tại: `main`

---

## 🔍 Các Bước Kiểm Tra

### Bước 1: Kiểm Tra File Có Được Push Lên GitLab Chưa

```bash
# Kiểm tra file có trong remote GitLab không
git ls-remote gitlab HEAD -- .gitlab-ci.yml

# Hoặc xem commit có trong remote không
git log gitlab/main --oneline -- .gitlab-ci.yml
```

**Nếu không có kết quả** → File chưa được push lên GitLab

**Giải pháp**:
```bash
git push gitlab main
```

---

### Bước 2: Kiểm Tra Trong GitLab UI

1. **Vào GitLab**: https://gitlab.com/ngoquangtruong2012004-group/datlichphongkham
2. **Kiểm tra file có trong repo**:
   - Vào **Repository → Files**
   - Tìm file `.gitlab-ci.yml`
   - Nếu không thấy → File chưa được push

3. **Kiểm tra Pipelines**:
   - Vào **CI/CD → Pipelines**
   - Xem có pipeline nào không
   - Nếu không có → Xem bước tiếp theo

4. **Kiểm tra Runners**:
   - Vào **Settings → CI/CD → Runners**
   - Xem có runner nào available không
   - Kiểm tra runner có tag `local` không
   - Kiểm tra runner có active (màu xanh) không

---

### Bước 3: Sửa Lỗi Syntax (Nếu Cần)

File `.gitlab-ci.yml` hiện tại có **PowerShell syntax** (`try-catch`, `Start-Sleep`, `$env:`). 

**Nếu runner là shell executor (Linux/Mac)**, cần sửa lại:

**Sửa phần deploy script** từ PowerShell sang Bash:

```yaml
deploy_job:
  script:
    - echo "===== DỪNG CÁC CONTAINER HIỆN CÓ ====="
    - docker-compose down || echo "Không có container nào để dừng"
    
    - echo "===== XÂY DỰNG VÀ KHỞI CHẠY CONTAINER ====="
    - docker-compose up -d --build
    
    - echo "===== KIỂM TRA TRẠNG THÁI CONTAINER ====="
    - docker ps -a
    
    - echo "===== ĐANG CHỜ CÁC DỊCH VỤ KHỞI ĐỘNG ====="
    - sleep 30
    
    - echo "===== KIỂM TRA TRẠNG THÁI DỊCH VỤ ====="
    - docker-compose ps
    
    - echo "===== KIỂM TRA HEALTH CHECK ====="
    - docker-compose exec -T mysql mysqladmin ping -h localhost -u root -p$MYSQL_ROOT_PASSWORD || echo "MySQL chưa sẵn sàng"
    
    - |
      echo "Kiểm tra Spring Boot App..."
      timeout 10 bash -c 'until curl -f http://localhost:8080/api/health || curl -f http://localhost:8080/actuator/health; do sleep 2; done' || echo "App chưa sẵn sàng"
    
    - echo "===== XEM LOGS ====="
    - docker-compose logs --tail=50 app
    - docker-compose logs --tail=20 mysql
```

---

## 🚀 Giải Pháp Nhanh Nhất

### Nếu File Chưa Được Push:

```bash
# Push file lên GitLab
git push gitlab main

# Hoặc push tất cả
git push gitlab main --all
```

### Nếu File Đã Được Push Nhưng Pipeline Không Chạy:

1. **Kiểm tra Runner**:
   - Vào GitLab → Settings → CI/CD → Runners
   - Enable runner có tag `local`
   - Đảm bảo runner đang active

2. **Test Pipeline Thủ Công**:
   - Vào GitLab → CI/CD → Pipelines
   - Click "Run pipeline"
   - Chọn branch `main`
   - Click "Run pipeline"

3. **Kiểm Tra Syntax**:
   - Vào GitLab → CI/CD → Editor
   - Click "Validate" để kiểm tra syntax

---

## 📋 Checklist Đầy Đủ

- [ ] File `.gitlab-ci.yml` đã được push lên GitLab
- [ ] File có trong GitLab Repository → Files
- [ ] Runner có tag `local` được enable cho project
- [ ] Runner đang active (màu xanh)
- [ ] Syntax của `.gitlab-ci.yml` đúng (không có lỗi)
- [ ] Branch `main` match với `only:` trong config
- [ ] Pipeline được trigger (tự động hoặc manual)

---

## 🔧 Command Nhanh

```bash
# 1. Kiểm tra file có trong remote
git ls-remote gitlab HEAD -- .gitlab-ci.yml

# 2. Push file lên GitLab
git push gitlab main

# 3. Xem logs của runner (trên máy chạy runner)
gitlab-runner --debug run

# 4. Kiểm tra runner status
gitlab-runner status
```

---

## ⚠️ Lưu Ý Quan Trọng

1. **PowerShell vs Bash**: 
   - File hiện tại dùng PowerShell syntax
   - Nếu runner là shell executor (Linux), cần sửa sang Bash
   - Nếu runner là PowerShell (Windows), giữ nguyên

2. **Tags Phải Khớp**:
   - Runner phải có tag `local`
   - Job phải có `tags: - local`

3. **Branch**:
   - Deploy job chỉ chạy trên `main` hoặc `master`
   - Build và Test chạy trên mọi branch

---

## 🎯 Hành Động Ngay

**Chạy các lệnh sau**:

```bash
# 1. Đảm bảo file được push
git push gitlab main

# 2. Kiểm tra trong GitLab UI
# - Vào: https://gitlab.com/ngoquangtruong2012004-group/datlichphongkham
# - Vào: CI/CD → Pipelines
# - Vào: Settings → CI/CD → Runners
```

Sau đó báo lại kết quả để tôi hỗ trợ tiếp!

