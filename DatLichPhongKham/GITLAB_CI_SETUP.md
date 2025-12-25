# Hướng Dẫn Setup GitLab CI/CD

## 📋 Tổng Quan

File `.gitlab-ci.yml` đã được tạo để tự động build và deploy ứng dụng khi push code lên GitLab.

---

## 🎯 Các Stages

Pipeline có 3 stages chính:

1. **Build**: Build Docker image và push lên GitLab Container Registry
2. **Test**: Chạy unit tests (optional)
3. **Deploy**: Deploy ứng dụng lên server

---

## ⚙️ Cấu Hình Cần Thiết

### 1. GitLab Variables (CI/CD Settings)

Vào **Settings → CI/CD → Variables** và thêm các biến sau:

#### Cho Development:
```
DEPLOY_SERVER=your-dev-server.com
DEPLOY_USER=deploy
DEPLOY_PATH=/path/to/your/app
SSH_PRIVATE_KEY=<your-ssh-private-key>
```

#### Cho Production:
```
DEPLOY_SERVER=your-prod-server.com
DEPLOY_USER=deploy
DEPLOY_PATH=/path/to/your/app
SSH_PRIVATE_KEY=<your-ssh-private-key>
```

#### GitLab Registry (Tự động có sẵn):
```
CI_REGISTRY_USER=<gitlab-username>
CI_REGISTRY_PASSWORD=<gitlab-token>
CI_REGISTRY=registry.gitlab.com
```

**Lưu ý**: 
- `SSH_PRIVATE_KEY` nên set là **Masked** và **Protected**
- Có thể tạo GitLab Deploy Token thay vì dùng username/password

---

## 🚀 Các Cách Deploy

### Cách 1: Deploy Với SSH (Khuyến nghị cho server riêng)

Sử dụng jobs: `deploy:development` hoặc `deploy:production`

**Yêu cầu**:
- Server có SSH access
- SSH key đã được setup
- Docker và docker-compose đã cài trên server
- File `docker-compose.yml` đã có trên server

**Cách setup**:

1. **Tạo SSH Key Pair**:
```bash
ssh-keygen -t rsa -b 4096 -C "gitlab-ci@yourdomain.com" -f gitlab-ci-key
```

2. **Copy public key lên server**:
```bash
ssh-copy-id -i gitlab-ci-key.pub deploy@your-server.com
```

3. **Thêm private key vào GitLab Variables**:
   - Vào GitLab → Settings → CI/CD → Variables
   - Key: `SSH_PRIVATE_KEY`
   - Value: Nội dung file `gitlab-ci-key` (private key)
   - Type: Variable
   - Flags: ✅ Masked, ✅ Protected

4. **Setup trên server**:
```bash
# SSH vào server
ssh deploy@your-server.com

# Tạo thư mục cho app
mkdir -p /opt/datlichphongkham
cd /opt/datlichphongkham

# Copy docker-compose.yml và các file cần thiết
# (hoặc clone repo và checkout branch tương ứng)
```

---

### Cách 2: Deploy Đơn Giản (GitLab Runner trên cùng server)

Sử dụng job: `deploy:simple`

**Yêu cầu**:
- GitLab Runner chạy trên cùng server với ứng dụng
- Runner có quyền truy cập Docker

**Cách setup**:

1. **Cài đặt GitLab Runner trên server**:
```bash
# Download và cài đặt GitLab Runner
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh" | sudo bash
sudo apt-get install gitlab-runner

# Đăng ký runner
sudo gitlab-runner register
```

2. **Cấu hình Runner**:
   - URL: `https://gitlab.com/`
   - Token: Lấy từ GitLab → Settings → CI/CD → Runners
   - Executor: `docker`
   - Default Docker image: `docker:24`

3. **Clone repo trên server**:
```bash
cd /opt
git clone https://gitlab.com/your-username/datlichphongkham.git
cd datlichphongkham
```

4. **Chạy pipeline**:
   - Push code lên GitLab
   - Pipeline sẽ tự động chạy và deploy

---

## 📝 Workflow

### Development Branch (develop)

1. Push code lên branch `develop`
2. Pipeline tự động:
   - ✅ Build Docker image
   - ✅ Run tests
   - ⏸️ Deploy (manual - cần click để deploy)

### Production Branch (main/master)

1. Merge code vào branch `main` hoặc `master`
2. Pipeline tự động:
   - ✅ Build Docker image
   - ✅ Run tests
   - ⏸️ Deploy (manual - cần click để deploy)

---

## 🔧 Tùy Chỉnh Pipeline

### Chỉnh Sửa Branches

Sửa trong `.gitlab-ci.yml`:

```yaml
only:
  - main
  - master
  - develop
  - feature/*  # Thêm branch pattern
```

### Bỏ Qua Test Stage

Nếu không muốn chạy tests, comment hoặc xóa job `test`:

```yaml
# test:
#   stage: test
#   ...
```

### Tự Động Deploy (Không cần manual)

Thay `when: manual` thành `when: on_success`:

```yaml
deploy:production:
  # ...
  when: on_success  # Thay vì manual
```

---

## 🐳 Docker Compose trên Server

### Cấu trúc thư mục trên server:

```
/opt/datlichphongkham/
├── docker-compose.yml
├── docker-compose.prod.yml
├── .env
└── mysql-init/ (nếu có)
```

### File .env trên server:

```bash
# Database
MYSQL_ROOT_PASSWORD=your-secure-password
MYSQL_DATABASE=phongkhambenh

# Application
APP_PORT=8080
JWT_SECRET=your-jwt-secret

# ... các biến khác
```

---

## 🔐 Security Best Practices

1. **SSH Keys**:
   - ✅ Sử dụng SSH key riêng cho CI/CD
   - ✅ Không commit private key vào repo
   - ✅ Set SSH key là Masked và Protected trong GitLab

2. **Secrets**:
   - ✅ Không hardcode passwords trong `.gitlab-ci.yml`
   - ✅ Sử dụng GitLab Variables cho sensitive data
   - ✅ Set variables là Protected và Masked

3. **Docker Registry**:
   - ✅ Sử dụng GitLab Container Registry
   - ✅ Set registry credentials trong Variables

---

## 📊 Monitoring Pipeline

### Xem Pipeline Status

1. Vào GitLab → CI/CD → Pipelines
2. Xem status của từng job
3. Click vào job để xem logs

### Debug Failed Jobs

```bash
# Xem logs trong GitLab UI
# Hoặc SSH vào server và check:
docker-compose logs
docker ps -a
```

---

## 🛠️ Troubleshooting

### Pipeline không chạy

1. **Kiểm tra GitLab Runner**:
   - Vào GitLab → Settings → CI/CD → Runners
   - Đảm bảo có runner đang active

2. **Kiểm tra tags**:
   - Runner phải có tag `docker` nếu job yêu cầu

### Build failed

1. **Kiểm tra Dockerfile**:
   ```bash
   docker build -t test .
   ```

2. **Kiểm tra logs**:
   - Xem logs trong GitLab CI/CD → Jobs

### Deploy failed

1. **Kiểm tra SSH connection**:
   ```bash
   ssh -i gitlab-ci-key deploy@your-server.com
   ```

2. **Kiểm tra Docker trên server**:
   ```bash
   ssh deploy@your-server.com
   docker ps
   docker-compose --version
   ```

3. **Kiểm tra permissions**:
   - User `deploy` phải có quyền chạy docker
   - Thêm user vào docker group: `sudo usermod -aG docker deploy`

---

## 📋 Checklist Setup

- [ ] GitLab repository đã được tạo
- [ ] GitLab Runner đã được cài đặt và đăng ký
- [ ] GitLab Variables đã được set (SSH keys, deploy info)
- [ ] Server đã được setup (Docker, docker-compose)
- [ ] SSH access đã được cấu hình
- [ ] File `.gitlab-ci.yml` đã được commit
- [ ] Test pipeline với branch develop
- [ ] Test deploy manual
- [ ] Setup production environment

---

## 🎯 Quick Start

### Lần đầu setup:

1. **Push code lên GitLab**:
```bash
git remote add origin https://gitlab.com/your-username/datlichphongkham.git
git push -u origin main
```

2. **Setup GitLab Variables** (như hướng dẫn trên)

3. **Setup GitLab Runner** (nếu chưa có)

4. **Push code và xem pipeline chạy**

5. **Click "Deploy" khi pipeline hoàn thành**

---

## 📝 Notes

- Pipeline sẽ tự động build khi push code
- Deploy là manual để tránh deploy nhầm
- Có thể thay đổi thành auto-deploy nếu muốn
- Production nên dùng `docker-compose.prod.yml`
- Development có thể dùng `docker-compose.yml`

---

**Tác giả**: Auto (AI Assistant)  
**Ngày tạo**: 2025-12-24  
**Phiên bản**: 1.0

