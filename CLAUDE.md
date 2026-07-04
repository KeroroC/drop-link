# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

Drop-Link 是一个极简公共文件共享平台。用户上传文件，获取分享链接，他人即可下载。支持文件过期、密码保护和下载次数统计。

## 技术栈

- **后端:** Spring Boot 3.2.5 + Spring Data JPA + SQLite + Spring Security
- **前端:** Vue 3 + Element Plus + Axios + Vite
- **Java 版本:** 17
- **部署方式:** 单 JAR 部署（前端构建后复制到后端静态资源目录）

## 开发命令

### 后端
```bash
cd backend
./mvnw spring-boot:run              # 启动后端，端口 8080
./mvnw test                         # 运行测试
./mvnw clean package -DskipTests    # 构建 JAR
```

### 前端
```bash
cd frontend
npm install                         # 安装依赖
npm run dev                         # 开发服务器，端口 5173（/api 代理到 localhost:8080）
npm run build                       # 生产构建
```

### 生产构建
```bash
./build.sh                          # 构建前端，复制到后端 static 目录，打包 JAR
java -jar backend/target/drop-link-0.0.1-SNAPSHOT.jar
```

## 架构说明

### 后端 (`backend/src/main/java/com/droplink/`)

标准 Spring Boot 分层架构：

- **controller/FileController** — REST 接口，路径前缀 `/api/files`。处理文件上传（multipart）、下载（含密码验证）、删除、列表查询和密码校验。
- **service/FileService** — 核心业务逻辑。文件存储使用 UUID 命名，存放在 `./uploads/`。内置危险扩展名黑名单。通过 Spring Security 的 `PasswordEncoder` 进行密码哈希。使用 `normalize()` + `startsWith()` 防止路径穿越。
- **entity/FileRecord** — JPA 实体，映射 `file_record` 表。字段：`fileId`（UUID）、`originalName`、`storedName`、`fileSize`、`uploadTime`、`expireTime`、`downloadCount`、`passwordHash`。
- **repository/FileRepository** — Spring Data JPA 仓库，包含过期文件查询和原子性下载计数递增。
- **config/SchedulerConfig** — 定时任务，每小时清理过期文件。
- **config/SecurityConfig** — Spring Security 配置。
- **config/WebConfig** — Web/CORS 配置。
- **exception/** — 全局异常处理器和自定义 `FileNotFoundException`。

### 前端 (`frontend/src/`)

- **App.vue** — 主布局，包含 `FileUploader` 和 `FileList` 组件。
- **components/FileUploader.vue** — 上传表单，支持可选的过期时间和密码字段。
- **components/FileList.vue** — 展示文件列表，支持下载/删除操作，处理密码保护文件的交互流程。
- **api/fileApi.js** — Axios API 客户端。基础路径 `/api/files`，超时 5 分钟（适配大文件上传）。下载使用 blob 响应并通过临时链接元素触发。

### 数据存储

- SQLite 数据库位于 `backend/data/file_share.db`（JPA `ddl-auto: update` 自动创建）
- 上传文件存储在 `backend/uploads/`（可通过 `application.yml` 的 `app.upload.dir` 配置）

## 关键设计决策

- **单 JAR 部署：** 前端单独构建后复制到 `backend/src/main/resources/static/`，JAR 同时提供 API 和静态文件服务。
- **无用户体系：** 面向公共使用，密码保护是针对单个文件的，而非用户级别。
- **文件过期：** 上传时可选 `expireHours` 参数，定时任务每小时清理过期文件及其物理存储。
- **安全措施：** 危险扩展名黑名单、路径穿越防护、密码文件哈希存储。
