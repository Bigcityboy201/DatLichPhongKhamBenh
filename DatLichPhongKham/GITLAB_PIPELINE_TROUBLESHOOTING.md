# Troubleshooting: Pipeline Không Chạy Sau Khi Push

## 🔍 Checklist Kiểm Tra

### 1. ✅ File .gitlab-ci.yml Có Tồn Tại?

**Kiểm tra**:

```bash
# Trong thư mục project
ls -la .gitlab-ci.yml
# Hoặc trên Windows
dir .gitlab-ci.yml
```

**Nếu không có**:

- File chưa được commit
- File bị ignore trong .gitignore

**Giải pháp**:

```bash
git add .gitlab-ci.yml
git commit -m "Add GitLab CI/CD pipeline"
git push origin main
```

---

### 2. ✅ File .gitlab-ci.yml Có Được Commit và Push?

**Kiểm tra**:

```bash
# Xem file có trong commit không
git log --oneline --all -- .gitlab-ci.yml

# Xem file có trong remote không
git ls-tree -r HEAD --name-only | grep .gitlab-ci.yml
```

**Nếu không có**:

```bash
# Add và commit file
git add .gitlab-ci.yml
git commit -m "Add GitLab CI/CD configuration"
git push origin main
```

---

### 3. ✅ File Có Bị Ignore?

**Kiểm tra .gitignore**:

```bash
cat .gitignore | grep gitlab
# Hoặc
type .gitignore | findstr gitlab
```

**Nếu có**:

- Xóa dòng ignore `.gitlab-ci.yml` trong `.gitignore`
- Commit lại

---

### 4. ✅ Runner Có Được Enable Cho Project?

**Kiểm tra trong GitLab UI**:

1. Vào project → **Settings → CI/CD → Runners**
2. Kiểm tra section **"Available specific runners"** hoặc **"Shared runners"**
3. Đảm bảo có runner nào đó đang **active** (màu xanh)

**Nếu không có runner**:

- Enable shared runners (nếu có)
- Hoặc enable specific runner cho project này
- Hoặc đăng ký runner mới

---

### 5. ✅ Tags Có Khớp?

**Kiểm tra**:

1. **Xem tags của runner**:

   - Vào GitLab → Settings → CI/CD → Runners
   - Xem tags của runner (ví dụ: `local`, `docker`)

2. **Xem tags trong .gitlab-ci.yml**:
   ```yaml
   build_job:
     tags:
       - local # Phải khớp với runner tag
   ```

**Nếu không khớp**:

- Sửa tags trong `.gitlab-ci.yml` để khớp với runner
- Hoặc thêm tag mới vào runner

---

### 6. ✅ Branch Có Được Trigger?

**Kiểm tra trong .gitlab-ci.yml**:

```yaml
deploy_job:
  only:
    - main # Chỉ chạy trên branch main
    - master
```

**Nếu bạn push lên branch khác** (ví dụ: `develop`):

- Pipeline sẽ không chạy job `deploy_job`
- Nhưng vẫn chạy `build_job` và `test_job`

**Giải pháp**:

- Push lên branch `main` hoặc `master`
- Hoặc sửa `only:` để include branch của bạn

---

### 7. ✅ Syntax Có Đúng?

**Kiểm tra syntax**:

1. **Trong GitLab UI**:

   - Vào project → CI/CD → Editor
   - GitLab sẽ highlight lỗi syntax nếu có

2. **Dùng GitLab CI Lint**:
   - Vào project → CI/CD → Editor → "Validate"
   - Hoặc dùng: https://gitlab.com/help/ci/lint

**Nếu có lỗi syntax**:

- Sửa lỗi trong `.gitlab-ci.yml`
- Commit và push lại

---

### 8. ✅ Runner Có Đang Chạy?

**Kiểm tra runner status**:

```bash
# Trên máy chạy runner
gitlab-runner status

# Hoặc
gitlab-runner list
```

**Nếu runner không chạy**:

```bash
# Start runner
gitlab-runner start

# Hoặc restart
gitlab-runner restart
```

---

## 🛠️ Các Bước Debug Chi Tiết

### Bước 1: Kiểm Tra File Có Trong GitLab

1. Vào GitLab → Project → Repository → Files
2. Tìm file `.gitlab-ci.yml`
3. Nếu không thấy → File chưa được push

### Bước 2: Kiểm Tra Pipelines

1. Vào GitLab → CI/CD → Pipelines
2. Xem có pipeline nào không
3. Nếu có pipeline nhưng bị "stuck" → Vấn đề về runner
4. Nếu không có pipeline → Vấn đề về file hoặc cấu hình

### Bước 3: Kiểm Tra Runners

1. Vào GitLab → Settings → CI/CD → Runners
2. Xem có runner nào available không
3. Kiểm tra runner có tag phù hợp không
4. Kiểm tra runner có active không (màu xanh)

### Bước 4: Test Pipeline Thủ Công

1. Vào GitLab → CI/CD → Pipelines
2. Click "Run pipeline"
3. Chọn branch
4. Click "Run pipeline"
5. Xem có lỗi gì không

---

## 🎯 Các Lỗi Thường Gặp

### Lỗi 1: "No runners available"

**Nguyên nhân**: Không có runner nào available cho project

**Giải pháp**:

1. Enable shared runners (nếu có)
2. Enable specific runner cho project
3. Đăng ký runner mới

### Lỗi 2: "This job is stuck"

**Nguyên nhân**:

- Runner không chạy
- Tags không khớp
- Runner không có quyền

**Giải pháp**:

```bash
# Kiểm tra runner
gitlab-runner status
gitlab-runner restart

# Kiểm tra tags
# Sửa .gitlab-ci.yml để tags khớp
```

### Lỗi 3: "Pipeline không xuất hiện"

**Nguyên nhân**:

- File .gitlab-ci.yml không tồn tại trong repo
- File bị ignore
- Syntax error

**Giải pháp**:

```bash
# Kiểm tra file có trong repo không
git ls-files | grep gitlab-ci

# Nếu không có, add và commit
git add .gitlab-ci.yml
git commit -m "Add CI/CD config"
git push
```

### Lỗi 4: "Job skipped"

**Nguyên nhân**:

- Branch không match với `only:` hoặc `except:`
- Conditions không thỏa mãn

**Giải pháp**:

- Kiểm tra `only:` và `except:` trong `.gitlab-ci.yml`
- Sửa để include branch của bạn

---

## ✅ Quick Fix Checklist

Chạy các lệnh sau để kiểm tra:

```bash
# 1. Kiểm tra file có tồn tại
ls -la .gitlab-ci.yml

# 2. Kiểm tra file có trong git
git ls-files | grep gitlab-ci

# 3. Kiểm tra file có trong commit gần nhất
git show HEAD:.gitlab-ci.yml

# 4. Kiểm tra file có trong remote
git ls-remote --heads origin

# 5. Push lại nếu cần
git add .gitlab-ci.yml
git commit -m "Add GitLab CI/CD"
git push origin main
```

---

## 🔧 Script Kiểm Tra Tự Động

Tạo file `check-pipeline.sh`:

```bash
#!/bin/bash

echo "=== Checking GitLab CI/CD Setup ==="

# Check file exists
if [ -f ".gitlab-ci.yml" ]; then
    echo "✅ .gitlab-ci.yml exists"
else
    echo "❌ .gitlab-ci.yml NOT FOUND"
    exit 1
fi

# Check file in git
if git ls-files | grep -q ".gitlab-ci.yml"; then
    echo "✅ .gitlab-ci.yml is tracked by git"
else
    echo "❌ .gitlab-ci.yml is NOT tracked by git"
    echo "Run: git add .gitlab-ci.yml"
    exit 1
fi

# Check file in last commit
if git show HEAD:.gitlab-ci.yml > /dev/null 2>&1; then
    echo "✅ .gitlab-ci.yml is in last commit"
else
    echo "❌ .gitlab-ci.yml is NOT in last commit"
    echo "Run: git add .gitlab-ci.yml && git commit -m 'Add CI/CD'"
    exit 1
fi

# Check syntax (basic)
if grep -q "stages:" .gitlab-ci.yml; then
    echo "✅ .gitlab-ci.yml has stages defined"
else
    echo "⚠️  .gitlab-ci.yml might have syntax issues"
fi

echo ""
echo "=== Next Steps ==="
echo "1. Push to GitLab: git push origin main"
echo "2. Check GitLab → CI/CD → Pipelines"
echo "3. Check GitLab → Settings → CI/CD → Runners"
```

---

## 📝 Tóm Tắt

**Nguyên nhân phổ biến pipeline không chạy**:

1. ❌ File `.gitlab-ci.yml` chưa được commit/push
2. ❌ File bị ignore trong `.gitignore`
3. ❌ Không có runner available
4. ❌ Tags không khớp giữa runner và job
5. ❌ Branch không match với `only:` trong config
6. ❌ Runner không chạy hoặc không active
7. ❌ Syntax error trong `.gitlab-ci.yml`

**Giải pháp nhanh nhất**:

```bash
# Đảm bảo file được commit và push
git add .gitlab-ci.yml
git commit -m "Add GitLab CI/CD pipeline"
git push origin main

# Sau đó kiểm tra trong GitLab UI
# GitLab → CI/CD → Pipelines
```

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0
