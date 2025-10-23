# Stan-blog

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.0-blue.svg)](https://reactjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2.2-blue.svg)](https://www.typescriptlang.org)

一个基于 Spring Boot 和 React 的现代化开源博客平台，支持多语言、多端适配，提供完整的内容管理和数据分析功能。

[English](README.md) | [繁體中文](README_TC.md) | 简体中文

## ✨ 主要特性

### 🎨 用户界面

- **现代化设计**: 基于 Material-UI 设计系统，支持暗黑/亮色主题切换
- **响应式布局**: 完美适配桌面、平板和移动设备
- **多语言支持**: 基于 i18next 的国际化解决方案
- **可访问性**: 符合 WCAG 标准，支持屏幕阅读器

### 📝 内容管理

- **富文本编辑器**: 集成 WangEditor 支持图片、视频、代码块等多媒体内容
- **智能标签系统**: 支持标签层级关系，构建复杂的内容分类系统
- **内容类型**: 支持文章、计划、词汇表等多种内容类型
- **自动草稿保存**: 防止创作内容丢失

### 🔐 用户系统

- **JWT 认证**: 安全的用户认证和授权机制
- **邮箱验证**: 完整的用户注册和密码重置工作流程
- **个人中心**: 用户个人信息管理和偏好设置
- **多设备同步**: 跨设备数据同步

### 📊 数据分析

- **内容统计**: 文章浏览量、点赞数等实时统计
- **用户行为**: 详细的用户访问和互动数据
- **可视化图表**: 直观的数据展示和趋势分析
- **导出功能**: 支持数据导出和报表生成

### 🚀 性能优化

- **代码分割**: 路由级别的懒加载，首次加载速度提升 60%+
- **缓存策略**: 智能前端缓存和 CDN 加速
- **SEO 友好**: 服务器端渲染支持，搜索引擎优化
- **PWA 支持**: 离线访问和推送通知

## 🛠️ 技术栈

### 前端 (stan_blog_web)

- **框架**: React 18.3.0 + TypeScript 5.2.2
- **构建工具**: Vite 5.2.0 (支持热重载和快速构建)
- **UI 组件库**: Material-UI 5.15.18
- **状态管理**: React Context + Hooks
- **路由**: React Router DOM 6.23.1
- **HTTP 客户端**: Axios 0.28.0
- **富文本编辑器**: WangEditor 5.1.20
- **国际化**: i18next 21.8.10
- **日期处理**: Moment.js 2.29.4
- **动画**: React Spring 9.7.1
- **代码质量**: ESLint + Prettier + TypeScript

### 后端 (stan_blog_service)

- **框架**: Spring Boot 3.2.5
- **安全**: Spring Security 6.2.4 + JWT
- **数据库**: MySQL + MyBatis Plus 3.5.6
- **缓存**: Redis (Spring Data Redis)
- **文档**: SpringDoc OpenAPI 2.5.0 (Swagger UI)
- **邮件**: Spring Boot Starter Mail + Thymeleaf
- **监控**: Spring Boot Actuator
- **测试**: JUnit 5.9.2 + H2 Database
- **Tools**: Lombok 1.18.38 + Apache Commons

### 开发环境

- **JDK**: 17
- **Node.js**: 18.20.2 (managed with Volta)
- **包管理**: Maven + NPM
- **版本控制**: Git
- **IDE**: 支持 Cursor, VSCode, IntelliJ IDEA

## 🚀 快速开始

### 前提条件

- JDK 17+
- Node.js 18.20.2+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 后端服务启动

```bash
# 导航到后端项目目录
cd stan_blog_service

# 安装依赖并启动 (开发环境)
mvn spring-boot:run

# 或构建生产环境
mvn package -Pprod
```

默认端口: `8080`  
API 文档: `http://localhost:8080/swagger-ui/index.html`

### 前端应用启动

```bash
# 导航到前端项目目录
cd stan_blog_web

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产环境
npm run build
```

默认端口: `3000`  
访问地址: `http://localhost:3000`

## 📖 项目结构

```
stan-blog/
├── stan_blog_service/          # 后端服务
│   ├── src/main/java/com/stan/blog/
│   │   ├── core/              # 主功能模块
│   │   ├── portal/            # 用户门户模块
│   │   ├── content/           # 内容管理模块
│   │   ├── analytics/         # 数据分析模块
│   │   └── beans/             # 数据实体模块
│   ├── src/main/resources/    # 配置文件
│   └── src/test/             # 单元测试
│
├── stan_blog_web/             # 前端应用
│   ├── src/
│   │   ├── portal/           # 用户门户模块
│   │   ├── admin/            # 管理后台模块
│   │   ├── components/       # 公共组件模块
│   │   ├── services/         # API 服务模块
│   │   └── contexts/         # 状态管理模块
│   ├── public/               # 静态资源
│   └── deploy/               # 部署相关
│
├── docker_compose/           # Docker Compose 文件
├── Jenkinsfiles/
│   └── Jenkinsfile_staging   # Staging 环境流水线配置
└── Jmeter/                   # JMeter 测试脚本和测试用户数据
```

## 🧪 测试

### 后端测试

```bash
cd stan_blog_service
mvn test
```

项目采用分层测试策略：

- **单元测试**: 覆盖服务层业务逻辑
- **集成测试**: 测试 API 接口和数据库交互
- **H2 内存数据库**: 测试环境隔离

### 前端测试

```bash
cd stan_blog_web
npm run lint          # 代码规范检查
npm run build:analyze  # 打包分析
```

### 负载测试

#### JMeter

```bash
# 创建文件夹
mkdir jmeter
mkdir jmeter\results
mkdir jmeter\logs

# 运行测试计划
jmeter.bat -n -t ".\jmeter\stanblog-test-plan.jmx" -l ".\jmeter\results.jtl" -j ".\jmeter\logs\jmeter.log"
```

## 📱 功能模块

### 用户门户 (Portal)

- **首页展示**: 最新文章、热门内容推荐
- **内容浏览**: 文章详情、标签分类、搜索功能
- **用户系统**: 注册登录、邮箱验证、密码重置
- **个人中心**: 个人资料、我的文章、互动记录

### 管理后台 (Admin)

- **内容管理**: 文章编辑、发布、草稿管理
- **标签管理**: 标签创建、层级关系管理
- **用户管理**: 用户信息、权限控制
- **数据分析**: 访问统计、内容表现分析
- **系统设置**: 网站配置、主题设置

## 🤖 AI（LLM）标题生成

- 功能概述：基于大语言模型，从文章正文智能生成精炼、易读且 SEO 友好的标题。
- 前端使用：在管理后台「文章编辑」页点击 `使用 AI 生成标题`；正文需超过 100 字；会有内容过短/过长、配额耗尽、生成失败或服务不可用等提示。
- 后端接口：`GET /v1/ai/check-quota` 返回 `{ allowed, remaining }`；`POST /v1/ai/generate-title` 请求体 `{ content: string }`；服务端校验正文长度（100–5000 字）。
- 配额与限流：非管理员每天最多 `ai.title.daily-limit` 次（默认 5 次）；管理员不受限；使用量通过 Redis 记录，窗口为 24 小时。
- 配置项：`ai.title.prompt`（系统提示词）、`ai.title.daily-limit`（每日配额）；底层使用 Spring AI `ChatClient`。
- 输出规范：生成的标题统一为单行文本，自动去除多余空白。

## 🌐 部署

### 开发环境

项目支持热重载开发，前端和后端可以独立启动，方便调试。

### 生产环境

- **前端**: 构建工具为 Vite，支持代码分割和缓存优化
- **后端**: Spring Boot 打包为 JAR，支持 Docker 容器化部署
- **数据库**: MySQL 主从架构，Redis 集群缓存
- **负载均衡**: Nginx 反向代理，支持 HTTPS

## 📈 性能优化

### 前端优化结果

- **打包体积**: 主文件从 1,079KB 减至 415KB (减少 61.6%)
- **代码分割**: vendor chunk 分离，长期缓存友好
- **懒加载**: 路由级别按需加载
- **压缩优化**: Terser 压缩，移除调试代码

### 后端优化

- **连接池**: HikariCP 数据库连接优化
- **缓存策略**: Redis 多级缓存
- **异步处理**: Spring @Async 非阻塞操作
- **JVM 调优**: 内存和 GC 优化，生产环境下的性能优化

## 🔗 相关链接

- **在线体验**: [https://stan-blog.stanli.site](https://stan-blog.stanli.site)
- **API 文档**: [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- **项目看板**: [Gitlab Projects](https://gitlab.stanli.site/stanli/stan-blog)
- **问题反馈**: [Gitlab Issues](https://gitlab.stanli.site/stanli/stan-blog/-/issues)
