# 石大视频共享平台 (Video Sharing Platform)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

一个基于 Spring Boot 的现代化视频共享平台，支持视频上传、播放、下载和管理功能。

## 特性

- **视频管理**: 支持MP4格式视频上传、播放、下载
- **智能搜索**: 多字段全文搜索，智能排序算法
- **用户系统**: 完整的用户注册、登录、权限管理
- **数据统计**: 实时播放/下载统计，可视化图表
- **安全保障**: Spring Security 安全框架，文件类型验证
- **响应式UI**: Bootstrap 5 响应式设计，支持移动端
- **离线运行**: 前端资源完全本地化，无需外网连接

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 应用框架
- **Java 21** - 编程语言
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问
- **MySQL 8.0** - 数据库
- **Flyway** - 数据库迁移
- **Thymeleaf** - 模板引擎

### 前端
- **Bootstrap 5.3.0** - UI框架 (本地化)
- **Chart.js 4.4.0** - 图表库 (本地化)
- **Bootstrap Icons** - 图标库 (本地化)

### 工具
- **FFmpeg** - 视频处理
- **Maven** - 构建工具

## 快速开始

### 环境要求

- Java 21+
- MySQL 8.0+
- FFmpeg
- Maven 3.6+

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/rsea2z/video-sharing-platform.git
   cd video-sharing-platform
   ```

2. **配置数据库**
   
   配置数据库连接信息有两种方式：
   
   **方式一：使用 secret 文件**
   
   在 `src/main/resources/` 目录下创建 `secrets.properties` 文件，内容如下：
    ```properties
    spring.datasource.username=YOUR_USERNAME
    spring.datasource.password=YOUR_PASSWORD
    ```
   
   **方式二：直接配置**
   
   修改 `src/main/resources/application.yml` 中的数据库连接信息，取消相关行的注释

3. **安装FFmpeg**
   
   确保系统中已安装FFmpeg和FFprobe，并添加到环境变量PATH中：
   ```bash
   # 验证安装
   ffmpeg -version
   ffprobe -version
   ```

4. **运行应用**
   ```bash
   # 使用Maven运行
   mvn spring-boot:run
   ```

5. **访问应用**
   
   打开浏览器访问: http://localhost:8080
   
   默认管理员账号:
   - 用户名: `admin`
   - 密码: `1234qwer`

## 使用说明

### 用户功能
- **注册/登录**: 新用户可以注册账号，注册后即可登录
- **视频浏览**: 所有用户都可以浏览视频列表和详情
- **视频播放**: 登录后可以播放视频
- **视频下载**: 登录后可以下载视频
- **视频上传**: 登录后可以上传MP4格式视频（最大200MB）
- **搜索功能**: 支持按标题、关键词、上传者搜索

### 管理员功能
- **用户管理**: 查看、编辑、删除用户
- **视频管理**: 删除任意视频
- **数据统计**: 查看播放/下载统计图表
- **系统监控**: 查看系统运行状态

### 视频要求
- **格式**: MP4容器
- **视频编码**: H.264
- **音频编码**: AAC
- **大小限制**: 最大200MB

## 项目结构

```
src/
├── main/
│   ├── java/com/videosite/
│   │   ├── VideoSiteApplication.java     # 主启动类
│   │   ├── config/                       # 配置类
│   │   ├── controller/                   # 控制器
│   │   ├── domain/entity/                # 实体类
│   │   ├── repository/                   # 数据访问层
│   │   ├── service/                      # 业务服务层
│   │   ├── security/                     # 安全配置
│   │   ├── dto/form/                     # 数据传输对象
│   │   └── util/                         # 工具类
│   └── resources/
│       ├── application.yml               # 应用配置
│       ├── db/migration/                 # 数据库迁移脚本
│       ├── static/                       # 静态资源
│       └── templates/                    # 页面模板
├── data/                                 # 数据存储目录
│   ├── videos/                          # 视频文件
│   └── thumbs/                          # 缩略图文件
└── logs/                                # 日志文件
```

## 配置说明

### 主要配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `app.storage.base-dir` | 文件存储根目录 | `./data` |
| `app.storage.video-dir` | 视频存储目录 | `videos` |
| `app.storage.thumb-dir` | 缩略图存储目录 | `thumbs` |
| `app.thumbnail.width` | 缩略图宽度 | `320` |
| `app.thumbnail.height` | 缩略图高度 | `180` |
| `spring.servlet.multipart.max-file-size` | 最大上传文件大小 | `200MB` |

## Acknowledgements
Expressions of gratitude are extended to Copilot.

## License
My pleasure if this project helps you.
