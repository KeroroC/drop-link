# Drop-Link

极简公共文件共享平台。上传文件，分享链接，随时下载。

## 功能特性

- 文件上传与下载
- 可选文件过期时间（自动清理）
- 可选文件密码保护
- 下载次数统计
- 危险文件类型过滤

## 技术栈

- **后端:** Spring Boot 3.2.5 + Spring Data JPA + SQLite + Spring Security
- **前端:** Vue 3 + Element Plus + Axios + Vite
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
| DELETE | /api/files/{fileId} | 删除文件 |
| POST | /api/files/{fileId}/download | 下载文件 |
| POST | /api/files/{fileId}/verify | 验证密码 |
| GET | /api/files/{fileId}/requires-password | 检查是否需要密码 |

### 上传参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 要上传的文件 |
| expireHours | Integer | 否 | 过期时间（小时），不填则永不过期 |
| password | String | 否 | 下载密码，不填则无密码保护 |

## 配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
app:
  upload:
    dir: ./uploads  # 文件上传目录

spring:
  servlet:
    multipart:
      max-file-size: 100MB      # 单文件大小限制
      max-request-size: 100MB   # 请求大小限制
```

## 项目结构

```
drop-link/
├── backend/          # Spring Boot 后端
│   ├── src/main/java/com/droplink/
│   │   ├── controller/    # REST 控制器
│   │   ├── service/       # 业务逻辑
│   │   ├── entity/        # JPA 实体
│   │   ├── repository/    # 数据仓库
│   │   ├── config/        # 配置类
│   │   └── exception/     # 异常处理
│   └── src/main/resources/
│       └── application.yml
├── frontend/         # Vue 3 前端
│   └── src/
│       ├── components/    # Vue 组件
│       └── api/           # API 客户端
├── build.sh          # 生产构建脚本
└── README.md
```
