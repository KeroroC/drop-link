# Drop-Link 设计文档

**日期：** 2026-07-04
**状态：** 已批准

## 1. 概述

构建一个极简的公共文件共享平台，允许任何人上传、浏览和下载文件。

**技术栈：**
- 前端：Vue 3 + Axios + Element Plus
- 后端：Spring Boot 3.x + Spring Data JPA + SQLite
- 存储：物理文件存于服务器本地磁盘，元数据存于 SQLite

**部署方式：** Vue 打包后放入 Spring Boot 的 `static/` 目录，单 JAR 部署。

## 2. 项目结构

```
drop-link/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/droplink/
│   │   ├── DropLinkApplication.java  # 启动类
│   │   ├── controller/
│   │   │   └── FileController.java   # 文件 API 控制器
│   │   ├── service/
│   │   │   └── FileService.java      # 文件业务逻辑
│   │   ├── entity/
│   │   │   └── FileRecord.java       # JPA 实体
│   │   ├── repository/
│   │   │   └── FileRepository.java   # 数据访问层
│   │   └── dto/
│   │       └── ApiResponse.java      # 统一响应包装
│   ├── src/main/resources/
│   │   ├── application.yml           # 配置文件
│   │   └── static/                   # Vue 打包产物放这里
│   └── pom.xml
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.js
│   │   └── components/
│   │       ├── FileUploader.vue      # 上传组件
│   │       └── FileList.vue          # 文件列表组件
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── README.md
```

## 3. 数据库设计

SQLite 自动创建，Spring Data JPA 根据 Entity 类自动生成表结构。

### 表结构：file_record

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | INTEGER | 主键，自增 | Primary Key, Auto Increment |
| file_id | VARCHAR(36) | UUID，对外暴露的下载标识 | Unique, Not Null |
| original_name | VARCHAR(255) | 用户上传时的原始文件名 | Not Null |
| stored_name | VARCHAR(255) | 服务器实际保存的文件名 | Not Null |
| file_size | BIGINT | 文件大小（字节） | Not Null |
| upload_time | TIMESTAMP | 上传时间 | Not Null |

## 4. 后端设计

### 4.1 依赖

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- sqlite-jdbc (SQLite 驱动)
- hibernate-community-dialects (Hibernate 6 SQLite 方言)

### 4.2 配置

```yaml
spring:
  datasource:
    url: jdbc:sqlite:data/file_share.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: false
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

app:
  upload:
    dir: ./uploads    # 可配置的上传目录
```

### 4.3 核心组件

**FileRecord 实体：**

```java
@Entity
@Table(name = "file_record")
public class FileRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 36)
    private String fileId;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadTime;
}
```

**FileService 核心逻辑：**
- `upload(MultipartFile file)` → 生成 UUID，写磁盘，存数据库
- `listAll()` → 按 uploadTime 倒序查询
- `getFileInfo(fileId)` → 查元数据
- `getStoredFile(fileId)` → 返回物理文件路径

**FileController 三个端点：**
- `POST /api/files/upload` → 调用 service.upload()
- `GET /api/files` → 调用 service.listAll()
- `GET /api/files/{fileId}/download` → 流式返回文件

### 4.4 统一响应格式

```java
public class ApiResponse<T> {
    private int code;       // 200 成功，其他为错误
    private String message; // 提示信息
    private T data;         // 响应数据
}
```

### 4.5 异常处理

全局 `@RestControllerAdvice` 捕获异常：
- 文件不存在 → 404
- 上传失败 → 500
- 其他异常 → 500

### 4.6 文件名编码处理

下载时设置响应头，处理中文文件名：

```java
String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
    .replaceAll("\\+", "%20");
response.setHeader("Content-Disposition",
    "attachment; filename*=UTF-8''" + encodedName);
```

### 4.7 安全约束

- 对外不暴露物理文件路径
- 所有下载必须通过 fileId 查库定位
- 文件名中的路径分隔符过滤（防目录遍历）
- 上传目录启动时自动创建

## 5. 前端设计

### 5.1 技术栈

- Vue 3 (Composition API)
- Element Plus（UI 组件库）
- Axios（HTTP 请求）
- Vite（构建工具）

### 5.2 核心组件

**FileUploader.vue** — 文件上传区：
- Element Plus 的 `el-upload` 组件，支持拖拽
- 上传时显示进度条（Axios `onUploadProgress`）
- 上传成功后触发事件通知父组件刷新列表

**FileList.vue** — 文件列表：
- Element Plus 的 `el-table` 展示文件信息
- 列：文件名、大小（自动转换 KB/MB/GB）、上传时间、操作
- 操作列：下载按钮
- 下载方式：`window.open('/api/files/{fileId}/download')`

**App.vue** — 主页面：
- 组合 FileUploader 和 FileList
- `onMounted` 时获取文件列表
- 上传成功后自动刷新列表

### 5.3 文件大小格式化

```javascript
function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}
```

### 5.4 Vite 代理配置（开发模式）

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

开发时前端请求自动代理到后端，无需 CORS 配置。

## 6. API 接口

### 6.1 获取文件列表

- **Method:** GET
- **Path:** `/api/files`
- **Response:**
```json
{
  "code": 200,
  "data": [
    {
      "fileId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "fileName": "项目文档.pdf",
      "fileSize": 1048576,
      "uploadTime": "2023-10-27T10:00:00"
    }
  ]
}
```

### 6.2 上传文件

- **Method:** POST
- **Path:** `/api/files/upload`
- **Content-Type:** multipart/form-data
- **Request:** 包含 `file` 字段
- **Response:**
```json
{
  "code": 200,
  "message": "上传成功"
}
```

### 6.3 下载文件

- **Method:** GET
- **Path:** `/api/files/{fileId}/download`
- **Response:** application/octet-stream（二进制流），附带文件名响应头

## 7. 构建与部署

### 开发模式

1. 后端：`cd backend && ./mvnw spring-boot:run`（端口 8080）
2. 前端：`cd frontend && npm run dev`（端口 5173，代理到 8080）

### 生产构建

1. `cd frontend && npm run build` → 生成 `dist/`
2. 将 `dist/` 内容复制到 `backend/src/main/resources/static/`
3. `cd backend && ./mvnw package` → 生成单个 JAR
4. `java -jar backend/target/drop-link.jar` → 前后端都在 8080 端口

## 8. 设计约束

- **隐私与安全：** 对外不暴露物理文件路径，所有下载请求必须经过 fileId 查库后定位文件
- **文件名编码：** 使用 RFC 5987 标准的 `filename*=UTF-8''` 格式处理中文文件名
- **清理机制：** 当前为永久存储，如需定期清理可通过 `@Scheduled` 定时任务实现
- **文件类型：** 不限制上传文件类型
- **存储路径：** 通过 `app.upload.dir` 配置项可自定义上传目录
