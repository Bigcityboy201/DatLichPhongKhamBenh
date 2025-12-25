# Fix: Maven Không Tìm Thấy pom.xml

## ❌ Lỗi

```
[ERROR] The goal you specified requires a project to execute but there is no POM in this directory
```

## 🔍 Nguyên Nhân

GitLab Runner clone repository nhưng `pom.xml` không có trong thư mục build.

## ✅ Đã Sửa

1. **Thêm Git Strategy**:

   ```yaml
   variables:
     GIT_STRATEGY: clone
     GIT_DEPTH: 0
   ```

2. **Thêm Debug Script**:

   - Kiểm tra file có tồn tại không
   - Force checkout nếu thiếu
   - Verify sau khi checkout

3. **Thêm Error Check**:
   - Kiểm tra `pom.xml` trước khi chạy Maven
   - Exit với error code nếu không tìm thấy

## 🚀 Bước Tiếp Theo

1. **Commit và Push**:

   ```bash
   git add .gitlab-ci.yml
   git commit -m "Fix: Add git strategy and pom.xml check"
   git push gitlab main
   ```

2. **Chạy Pipeline Lại**:

   - Vào GitLab → CI/CD → Pipelines
   - Click "Run pipeline" hoặc đợi push tự động trigger

3. **Xem Logs**:
   - Xem phần `before_script` để debug
   - Kiểm tra xem `pom.xml` có được tìm thấy không

## 🔧 Nếu Vẫn Lỗi

### Kiểm Tra pom.xml Có Trong GitLab

1. Vào GitLab → Repository → Files
2. Tìm file `pom.xml`
3. Nếu không thấy → File chưa được push

### Push pom.xml Nếu Thiếu

```bash
# Kiểm tra
git ls-files | grep pom.xml

# Nếu có, push lên GitLab
git push gitlab main

# Nếu không có trong git, add và push
git add pom.xml
git commit -m "Add pom.xml"
git push gitlab main
```

### Kiểm Tra GitLab Runner

```bash
# Trên máy chạy runner
gitlab-runner --debug run

# Xem logs
gitlab-runner --debug run 2>&1 | tee runner.log
```

---

## 📝 File .gitlab-ci.yml Đã Được Cập Nhật

- ✅ Thêm `GIT_STRATEGY: clone`
- ✅ Thêm `GIT_DEPTH: 0`
- ✅ Thêm debug script
- ✅ Thêm force checkout
- ✅ Thêm error check

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-25
