# Fix: Xử Lý File .gitlab-ci.yml Trùng Lặp

## 🔍 Vấn Đề

Bạn có **2 file `.gitlab-ci.yml`**:

1. ✅ **Root**: `.gitlab-ci.yml` (file chính - GitLab sẽ dùng file này)
2. ⚠️ **Subdirectory**: `DatLichPhongKham/.gitlab-ci.yml` (file cũ - có thể gây confusion)

## ✅ Giải Pháp

### GitLab Chỉ Dùng File Ở Root

GitLab **chỉ tìm và dùng file `.gitlab-ci.yml` ở root** của repository. File trong subdirectory sẽ bị bỏ qua.

**Vì vậy**: File trong `DatLichPhongKham/` **KHÔNG phải nguyên nhân gây lỗi**.

## 🎯 Nguyên Nhân Thực Sự

Lỗi `pom.xml not found` xảy ra vì:

1. **Cấu trúc repo trên GitLab khác với local**:

   - Trên GitLab: `pom.xml` có thể ở trong `DatLichPhongKham/`
   - Local: `pom.xml` ở root

2. **GitLab Runner clone repo**:
   - Clone vào thư mục: `C:\GitLab-Runner\builds\...\datlichphongkham\`
   - Ở root của repo clone
   - Không tìm thấy `pom.xml` ở root

## ✅ Đã Sửa Trong .gitlab-ci.yml

File `.gitlab-ci.yml` hiện tại đã có logic để:

1. Tìm `pom.xml` ở root
2. Nếu không có, tìm trong `DatLichPhongKham/`
3. Tự động `cd` vào đúng thư mục

## 🧹 Dọn Dẹp (Tùy Chọn)

Nếu muốn xóa file trong subdirectory để tránh confusion:

```bash
# Kiểm tra file có tồn tại không
git ls-files | grep "DatLichPhongKham/.gitlab-ci.yml"

# Nếu có, xóa khỏi git (không xóa file local nếu có)
git rm --cached DatLichPhongKham/.gitlab-ci.yml

# Commit
git commit -m "Remove duplicate .gitlab-ci.yml from subdirectory"

# Push
git push gitlab main
```

**Lưu ý**: Không bắt buộc, vì GitLab không dùng file đó.

## 📝 Kết Luận

- ✅ **File trùng lặp KHÔNG phải nguyên nhân gây lỗi**
- ✅ **GitLab chỉ dùng file ở root**
- ✅ **File `.gitlab-ci.yml` hiện tại đã có logic xử lý đúng**
- ✅ **Chỉ cần đảm bảo file ở root được push lên GitLab**

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-25
