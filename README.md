# Drop-Link

极简公共文件共享平台。上传文件，分享链接，随时下载。

## 技术栈

- **后端:** Spring Boot 3.x + Spring Data JPA + SQLite
- **前端:** Vue 3 + Element Plus + Axios
- **部署:** 单 JAR 部署（前端打包进后端）

## 快速开始

### 开发模式

**启动后端：**
```bash
cd backend
./mvnw spring-boot:run
```

**启动前端：**
```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
./build.sh
java -jar backend/target/drop-link-0.0.1-SNAPSHOT.jar
```

访问 http://localhost:8080

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/files | 获取文件列表 |
| POST | /api/files/upload | 上传文件 |
| GET | /api/files/{fileId}/download | 下载文件 |

## 配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
app:
  upload:
    dir: ./uploads  # 文件上传目录
```

## 项目结构

```
drop-link/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── build.sh          # 生产构建脚本
└── README.md
```