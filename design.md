一、总体架构与运行方式
- 技术栈
  - 后端：Spring Boot 3.2.0（Web、Security、Data JPA、Validation）、Java 21、Maven 
  - 数据库：MySQL 8.0，字符集 utf8mb4，排序规则 utf8mb4_0900_ai_ci，时区 Asia/Shanghai
  - 模板与样式：Thymeleaf + Bootstrap 5（本地文件）；图表：Chart.js（本地文件）
  - 日志：SLF4J + Logback（默认配置）
  - 数据迁移：Flyway（负责表结构初始化与变更）
- 运行方式
  - 单体可执行 jar 本地运行
  - 文件存储：在项目根目录下的 data 目录
    - 视频文件：<项目根>/data/videos/yyyy/MM/dd/uuid.mp4
    - 缩略图：<项目根>/data/thumbs/yyyy/MM/dd/uuid.jpg
- 外部依赖
  - ffmpeg/ffprobe 必须可执行且可在 PATH 中找到
  - 启动时检查 ffmpeg/ffprobe，不可用则直接终止启动（保持平台功能一致性）

二、功能范围与权限矩阵
- 匿名用户
  - 可浏览首页、视频列表页、视频详情页（含标题、描述、关键词、上传者、缩略图等）
  - 不可播放、不可下载
- 登录用户（ROLE_USER）
  - 可播放视频（受鉴权保护）
  - 可下载视频（受鉴权保护）
  - 可上传视频（单文件 MP4，≤200MB）
  - 可删除本人上传的视频
- 管理员（ROLE_ADMIN）
  - 具备所有用户权限
  - 可删除任意视频
  - 可访问后台大屏与统计报表
- 登录与注册
  - 表单登录，注册即生效，无验证码
  - 初始管理员账号：用户名 admin，密码 1234qwer（首次启动自动创建）

三、页面与导航信息架构
- 公共导航
  - 首页（视频列表，按创建时间倒序）
  - 搜索框（单一输入，匹配标题/关键词/上传者）
  - 登录/注册/退出
  - 上传入口（登录后可见）
  - 管理入口（管理员可见）
- 主要页面
  - 视频列表页：分页（每页 12 条），缩略图 + 标题 + 上传者 + 创建时间
  - 视频详情页：展示标题、描述、关键词（标准化后）、上传者、创建时间、时长、浏览/下载次数、缩略图；播放按钮（需登录后显示播放器）
  - 播放区：HTML5 video 标签，请求带 Range 头（受鉴权）
  - 上传页：标题、描述、关键词输入框、选择 mp4 文件
  - 管理大屏：总浏览、总下载、Top10（历史累计）、按日/周趋势曲线
- 下载行为
  - 通过受保护的下载接口返回附件（Content-Disposition），触发下载计数

四、搜索与排序语义（确定性）
- 单一搜索框，对标题/关键词/上传者三项同时匹配
- 关键词分隔符：逗号(,)、中文逗号(，)、分号(;)、空格、井号(#)
- 标准化：对输入与存储均进行 trim、转小写、去重
- 匹配定义
  - 一个视频命中某个“词”的条件：该词出现在标题 或 关键词 或 上传者（用户名）中（均使用小写比较，模糊包含匹配）
- 返回顺序
  - 第一层：先返回“命中全部词”的结果（AND）
  - 第二层：再返回“命中任意词”的结果（OR）但排除第一层已出现的项
  - 各层内排序：先按命中词数量降序（匹配多的排前），再按创建时间倒序
- 分页：每页 12 条

五、上传与文件处理规范
- 接受文件类型：仅 mp4 容器；通过 ffprobe 校验容器与流信息
- 编解码要求：H.264 视频 + AAC 音频（若任一不符合，判为不合规，拒绝上传）
- 文件大小限制：单文件最大 200MB（请求体与单文件双重限制）
- 保存策略
  - 物理文件名：UUID.mp4；目录：yyyy/MM/dd
  - 数据库保留原始文件名，用于展示与下载时的默认文件名
- 缩略图与时长
  - 使用 ffprobe 读取时长（秒）
  - 取第 5 秒截帧生成 320x180 的 jpg 缩略图；若视频不足 5 秒，取时长中位数位置
  - 若缩略图生成失败视为上传失败（保持平台一致性）
- 安全
  - 文件保存至 data 目录，非静态目录，不直接暴露；所有播放/下载由受控接口输出
  - 严禁将上传目录映射为静态资源路径

六、播放、下载与统计口径
- 播放
  - 仅登录用户可请求视频流接口
  - 必须支持 Range 请求，返回 206，按分片流式传输（服务器端按 1MB 左右的分块响应）
  - 播放计数触发：当首次收到包含字节 0 的 Range 请求即计 1 次（不去重）
- 下载
  - 仅登录用户可请求下载接口
  - 每次成功响应下载均计 1 次（不去重）
- 计数持久化与日聚合
  - 实时字段：video.views_total、video.downloads_total 使用数据库原子自增
  - 日聚合表：对每次播放/下载同时写入或 upsert 当天的计数 +1，便于大屏统计
  - Top10：按历史累计 views_total 或 downloads_total 排序（历史口径已确定为累计）

七、删除与一致性
- 权限：上传者本人和管理员可删除视频
- 删除行为：删除数据库记录并物理删除视频与缩略图文件（不可恢复）
- 原子性：先逻辑检查权限，再物理文件删除，最后删除数据库记录；若物理文件缺失，忽略并继续保证记录被清理
- 旁路引用：禁止其他表对 video 的外键产生残留（使用外键约束级联删除或应用层保证）

八、数据模型（表结构与约束）
- user 表（用户）
  - id: BIGINT 主键，自增
  - username: VARCHAR(20) 唯一，索引，规则：3-20 位，字母数字下划线
  - password_hash: VARCHAR(100)，BCrypt 存储
  - email: VARCHAR(255)，可空（选填）
  - role: ENUM('USER','ADMIN') 或 VARCHAR(20) + 约束，默认 USER
  - enabled: TINYINT(1) 默认 1
  - created_at: DATETIME(3)
  - 索引：UNIQUE(username)
- video 表（视频）
  - id: BIGINT 主键，自增
  - title: VARCHAR(255) 非空（用于展示与搜索）
  - description: TEXT 可空
  - keywords: VARCHAR(1000) 存标准化后的关键词，分隔符统一为英文逗号
  - original_filename: VARCHAR(255) 原始文件名（下载默认名）
  - storage_path: VARCHAR(500) 相对路径（如 videos/2025/09/01/uuid.mp4）
  - thumb_path: VARCHAR(500) 相对路径（如 thumbs/2025/09/01/uuid.jpg）
  - uploader_id: BIGINT 非空（FK -> user.id）
  - size_bytes: BIGINT 非空
  - duration_seconds: INT 非空
  - views_total: BIGINT 默认 0
  - downloads_total: BIGINT 默认 0
  - visibility: VARCHAR(20) 默认 'PUBLIC'（预留字段）
  - created_at: DATETIME(3)
  - 索引：INDEX(title)、INDEX(uploader_id)、INDEX(created_at)、INDEX(views_total)、INDEX(downloads_total)
  - 搜索优化：对 title、keywords、uploader.username 做 LIKE 模糊；考虑到开发快与轻量，不使用额外全文检索组件
- video_daily_stats 表（日聚合）
  - id: BIGINT 主键，自增
  - video_id: BIGINT 非空（FK -> video.id）
  - stat_date: DATE 非空
  - views: INT 默认 0
  - downloads: INT 默认 0
  - 约束：UNIQUE(video_id, stat_date)
  - 用途：按日聚合曲线（周统计由最近 7 天聚合得出）
- 约束与外键
  - video.uploader_id 外键到 user.id（删除用户时如不允许级联删除，可禁用级联并由管理流程先迁移/删除视频）
  - video_daily_stats.video_id 外键到 video.id，ON DELETE CASCADE（删除视频自动清理历史统计）

九、搜索实现细节（SQL 层面的确定性策略）
- 输入解析：按分隔符将输入拆分为 tokens，trim、小写、去重，空词剔除
- AND 集合：满足“每个 token 至少命中 title 或 keywords 或 uploader.username”的视频集合
- OR 集合：满足“至少一个 token 命中”的集合
- 合并顺序：先 AND 结果，再追加 OR 结果（去重）
- 排序策略
  - 计算每条记录的 token 命中数（title/keywords/uploader 三字段的合并匹配去重计数）
  - AND 集合内按命中数降序，再按 created_at 降序
  - OR 集合内按命中数降序，再按 created_at 降序
- 索引与性能
  - title、uploader_id 建普通索引
  - 关键词字段 LIKE 查询无法有效使用索引，接受在课程项目规模下的性能表现
  - 分页使用数据库分页（limit/offset），每页 12 条

十、后端接口与页面交互映射（无代码，接口语义）
- 页面渲染
  - GET /            首页列表（分页、排序、搜索框）
  - GET /videos      列表（分页、搜索查询参数 q）
  - GET /videos/{id} 详情页（匿名可访问，播放器区域仅登录可见）
  - GET /upload      上传表单（登录）
  - POST /upload     提交上传（登录）
  - GET /admin/dashboard 管理大屏（管理员）
- 播放与下载
  - GET /stream/{id} 视频流（登录，必须支持 Range；接收到包含字节0的 Range 请求即记播放+1，并写入日聚合）
  - GET /download/{id} 下载（登录，返回附件，成功即记下载+1，并写入日聚合）
- 账户
  - GET /login、POST /login（表单）
  - GET /register、POST /register（表单）
  - POST /logout
- 视频管理
  - POST /videos/{id}/delete 删除（登录，需本人或管理员）

十一、业务流程（端到端）
- 注册
  - 用户名、密码（8-64，含字母与数字）、邮箱选填；校验通过后写库，赋予 USER 角色
- 登录
  - 表单登录；成功后跳转回上次访问页或首页
- 上传
  1) 表单提交：标题、描述、关键词、mp4 文件
  2) 服务端校验：登录态、文件大小、扩展名、Content-Type
  3) 落盘：生成 uuid 与 yyyy/MM/dd 目录，流式写入，避免内存堆积
  4) ffprobe 检查容器与编解码（H.264/AAC），读取时长
  5) ffmpeg 生成 320x180 缩略图，取第 5 秒（或中位数）
  6) 关键词标准化存储（小写、去重、英文逗号拼接）
  7) 写入 video 表记录
- 播放
  1) 登录校验
  2) 验证视频存在与可见性
  3) 解析 Range 请求，按块返回 206
  4) 若本次请求包含字节 0 则计 views_total +1，写/更新当日日聚合（views +1）
- 下载
  1) 登录校验
  2) 返回附件下载并计 downloads_total +1，写/更新当日日聚合（downloads +1）
- 删除
  1) 鉴权（本人或管理员）
  2) 删除物理文件（视频、缩略图，缺失忽略）
  3) 删除数据库记录（video，级联清理日聚合）

十二、安全与合规
- 身份鉴权：Spring Security 会话/表单登录
- 授权规则
  - 匿名允许访问：列表、详情、注册、登录、静态资源、首页
  - 登录后才允许：流媒体 /stream/{id}、下载 /download/{id}、上传、删除
  - 管理员：/admin/**
- CSRF：对表单启用 CSRF 防护（上传、删除、注册、登录）
- 输入验证：JSR-380 注解校验 + 服务端二次校验（标题长度、关键词总长度、描述长度）
- 目录穿越：禁止任何用户输入参与物理路径拼接；仅使用服务端生成的 UUID 与规范化目录
- MIME/内容校验：通过 ffprobe 报告作为最终判定依据（拒绝非 MP4/H.264/AAC）
- 速率控制：不实施限速与去重（按确认要求执行）

十三、配置说明（实际实现）
- MySQL 连接（实际配置）
  - 主机：10.200.21.33（已配置）
  - 端口：3306
  - 数据库名：video_site
  - 用户名/密码：root / ***REDACTED***（已配置）
  - 连接参数：使用时区 serverTimezone=Asia/Shanghai，字符集 useUnicode=true&characterEncoding=utf8&useSSL=false
- JPA
  - 方言：MySQL 8
  - DDL 策略：由 Flyway 管理表结构，不启用自动建表
- Multipart 上传限制
  - 单文件最大 200MB
  - 单请求最大 200MB（与单文件一致，防止批量绕过）
- 文件根目录
  - baseDir = ./data
  - videos 子目录 = baseDir/videos
  - thumbs 子目录 = baseDir/thumbs
- 时区与时间
  - 应用默认时区：Asia/Shanghai；展示与存储均按本地时间
- 启动检查
  - 开机自检 ffmpeg/ffprobe，不存在则终止启动并记录错误日志
- 静态资源（本地化）
  - Bootstrap 5.3.0 CSS/JS 存储在 /vendor/bootstrap/
  - Bootstrap Icons 1.10.0 存储在 /vendor/bootstrap-icons/
  - Chart.js 4.4.0 存储在 /vendor/chart.js/
  - 所有前端资源本地化，无外网依赖

十四、统计与大屏实现细节
- 指标口径
  - 总浏览：video.views_total 汇总
  - 总下载：video.downloads_total 汇总
  - Top10：按历史累计 views_total 或 downloads_total 排序（页面可切换维度）
  - 按日趋势：来自 video_daily_stats（近 7 天/近 30 天曲线）
- 数据提供方式
  - 页面 SSR + 异步接口提供 JSON（/admin/api/stats/...），Chart.js 通过 AJAX 拉取
- 性能
  - 日聚合写入采用“存在则更新，不存在则插入”的幂等策略（利用唯一键 video_id+stat_date）
  - 管理页查询加缓存（应用级 5~10 分钟）以降低频繁刷新压力

十五、日志与审计
- 上传、删除、播放、下载均记录审计日志（操作人、视频ID、时间、IP）
- 错误日志记录异常堆栈（不回显给用户）

十六、项目脚手架清单（实际实现的目录与分层）
- 分层（已实现）
  - domain/entity：实体类（User、Video、VideoDailyStats）
  - repository：JPA 仓储接口（UserRepository、VideoRepository、VideoDailyStatsRepository）
  - service：业务服务（VideoService、StorageService、TranscodeService、StatsService、UserService）
  - controller：页面与接口控制器（HomeController、VideoController、AuthController、AdminController）
  - security：Security 配置、CustomUserDetailsService
  - config：应用配置（SecurityConfig）
  - dto/form：表单与视图对象（RegisterForm、UploadForm）
  - util：关键词解析、日期工具、文件工具
- 资源目录（已实现）
  - templates/：Thymeleaf 页面（layout.html、index.html、auth/、videos/、admin/dashboard.html）
  - static/：本地静态资源
    - vendor/bootstrap/：Bootstrap 5.3.0 CSS/JS
    - vendor/bootstrap-icons/：Bootstrap Icons 1.10.0
    - vendor/chart.js/：Chart.js 4.4.0
    - css/style.css：自定义样式
    - js/app.js：自定义脚本
  - db/migration/：Flyway 脚本（V1__init_schema.sql）
  - application.yml：应用配置（MySQL连接、文件路径、上传限制等）
- 实际依赖
  - spring-boot-starter-web、spring-boot-starter-thymeleaf、spring-boot-starter-security、spring-boot-starter-data-jpa、spring-boot-starter-validation
  - thymeleaf-layout-dialect、thymeleaf-extras-springsecurity6
  - mysql-connector-j
  - flyway-core、flyway-mysql
  - lombok（减少样板代码）
  - commons-io（文件操作辅助）
  - 测试：spring-boot-starter-test、spring-security-test

十七、表单与校验细则
- 用户名：3-20，字母/数字/下划线，唯一
- 密码：8-64，必须包含字母与数字
- 邮箱：选填，若填入必须为有效邮箱格式
- 标题：1-255
- 描述：0-5000
- 关键词：总长度≤1000，按规则分隔与标准化存储
- 上传文件：仅 .mp4，≤200MB

十八、错误处理与用户提示
- 全局异常处理：返回友好错误页（包含错误码与简明描述）
- 上传失败：清晰提示具体原因（格式不合规、大小超限、ffmpeg 失败、IO 失败等）
- 播放/下载鉴权失败：统一跳转登录页

十九、测试计划（要点）
- 单元测试：关键词解析、权限判断、统计写入（含并发安全的自增）、删除时文件与记录一致性
- 集成测试：上传-缩略图生成-播放-下载全链路；搜索语义（AND 优先、OR 其次、排序正确）
- 手工测试：大于 200MB 文件拒绝；非 H.264/AAC 拒绝；无 ffmpeg 启动失败

二十、部署与运行（实际环境）
- 数据库环境
  - MySQL 8 已部署在 10.200.21.33:3306
  - 数据库名：video_site，用户名：root，密码：***REDACTED***
  - 字符集：utf8mb4，时区：Asia/Shanghai
- 外部依赖
  - ffmpeg 和 ffprobe 已安装并加入 PATH（用于视频处理和缩略图生成）
- 文件存储
  - 配置 baseDir=./data，应用启动时自动创建目录结构
  - videos/ 和 thumbs/ 子目录按日期分层存储
- 前端资源
  - 所有 Bootstrap、Icons、Chart.js 资源已本地化到 /vendor/ 目录
  - 支持离线环境，无需外网连接
- 首次启动
  - Flyway 自动执行数据库迁移脚本
  - 自动创建管理员账号：admin/1234qwer
- 访问方式
  - 本地访问：http://localhost:8080
  - 功能验证：注册、登录、上传视频、播放、下载、管理后台
