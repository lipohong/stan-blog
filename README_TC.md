# Stan-blog

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.0-blue.svg)](https://reactjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2.2-blue.svg)](https://www.typescriptlang.org)

一個基於 Spring Boot 與 React 的現代化開源部落格平台，支援多語系、多端適配，並提供完整的內容管理與資料分析功能。

[English](README.md) | [简体中文](README_SC.md)

## ✨ 主要特色

### 🎨 用家界面

- **現代化設計**：採用 Material-UI 設計系統，支援深色／淺色主題切換
- **響應式版型**：完美適配桌面、平板與行動裝置
- **多語系支援**：基於 i18next 的國際化解決方案
- **無障礙**：符合 WCAG 標準，支援螢幕閱讀器

### 📝 內容管理

- **豐富文字編輯器**：整合 WangEditor，支援圖片、影片、程式碼區塊等多媒體內容
- **智慧標籤系統**：支援標籤階層關係，建構複雜的內容分類
- **內容型別**：支援文章、計畫、詞彙表等多種型別
- **自動草稿儲存**：避免創作內容遺失

### 🔐 使用者系統

- **JWT 驗證**：安全的身分驗證與授權機制
- **電子郵件驗證**：完整的註冊與重設密碼流程
- **個人中心**：個人資料管理與偏好設定
- **多裝置同步**：跨裝置資料同步

### 📊 資料分析

- **內容統計**：文章瀏覽量、按讚數等即時統計
- **使用者行為**：詳細的造訪與互動資料
- **視覺化圖表**：直覺的資料呈現與趨勢分析
- **匯出功能**：支援資料匯出與報表產生

### 🚀 效能最佳化

- **程式碼分割**：路由層級的延遲載入，初次載入速度提升 60%+
- **快取策略**：智慧前端快取與 CDN 加速
- **SEO 友善**：支援伺服器端渲染，搜尋引擎最佳化
- **PWA 支援**：離線存取與推播通知

## 🛠️ 技術堆疊

### 前端（stan_blog_web）

- **框架**：React 18.3.0 + TypeScript 5.2.2
- **建置工具**：Vite 5.2.0（支援熱重載與快速建置）
- **UI 元件庫**：Material-UI 5.15.18
- **狀態管理**：React Context + Hooks
- **路由**：React Router DOM 6.23.1
- **HTTP 客戶端**：Axios 0.28.0
- **豐富文字編輯器**：WangEditor 5.1.20
- **國際化**：i18next 21.8.10
- **日期處理**：Moment.js 2.29.4
- **動畫**：React Spring 9.7.1
- **程式碼品質**：ESLint + Prettier + TypeScript

### 後端（stan_blog_service）

- **框架**：Spring Boot 3.2.5
- **安全**：Spring Security 6.2.4 + JWT
- **資料庫**：MySQL + MyBatis Plus 3.5.6
- **快取**：Redis（Spring Data Redis）
- **文件**：SpringDoc OpenAPI 2.5.0（Swagger UI）
- **郵件**：Spring Boot Starter Mail + Thymeleaf
- **監控**：Spring Boot Actuator
- **測試**：JUnit 5.9.2 + H2 Database
- **工具**：Lombok 1.18.38 + Apache Commons

### 開發環境

- **JDK**：17
- **Node.js**：18.20.2（以 Volta 管理）
- **套件管理**：Maven + NPM
- **版本控制**：Git
- **IDE**：支援 Cursor、VSCode、IntelliJ IDEA

## 🚀 快速開始

### 先決條件

- JDK 17+
- Node.js 18.20.2+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 啟動後端服務

```bash
# 前往後端專案目錄
cd stan_blog_service

# 安裝相依並啟動（開發環境）
mvn spring-boot:run

# 或建置正式環境
mvn package -Pprod
```

預設連接埠：`8080`
API 文件：`http://localhost:8080/swagger-ui/index.html`

### 啟動前端應用

```bash
# 前往前端專案目錄
cd stan_blog_web

# 安裝相依套件
npm install

# 啟動開發伺服器
npm run dev

# 建置正式環境
npm run build
```

預設連接埠：`3000`
存取位址：`http://localhost:3000`

## 📖 專案結構

```
stan-blog/
├── stan_blog_service/          # 後端服務
│   ├── src/main/java/com/stan/blog/
│   │   ├── core/              # 核心功能模組
│   │   ├── portal/            # Portal APIs
│   │   ├── content/           # 內容管理
│   │   ├── analytics/         # 資料分析
│   │   └── beans/             # 資料實體
│   ├── src/main/resources/    # 設定檔
│   └── src/test/              # 單元測試
│
└── stan_blog_web/             # 前端應用
    ├── src/
    │   ├── portal/           # 使用者入口
    │   ├── admin/            # 後台儀表板
    │   ├── components/       # 共用元件
    │   ├── services/         # API 服務
    │   └── contexts/         # 狀態管理
    ├── public/               # 靜態資產
    └── deploy/               # 部署相關
```

## 🧪 測試

### 後端測試

```bash
cd stan_blog_service
mvn test
```

專案採分層測試策略：

- **單元測試**：涵蓋服務層商業邏輯
- **整合測試**：測試 API 與資料庫互動
- **H2 記憶體資料庫**：測試環境隔離

### 前端測試

```bash
cd stan_blog_web
npm run lint           # 程式碼規範檢查
npm run build:analyze  # 打包分析
```

## 📱 功能模組

### 使用者入口（Portal）

- **首頁展示**：最新文章、熱門內容推薦
- **內容瀏覽**：文章詳情、標籤分類、搜尋功能
- **使用者系統**：註冊登入、電子郵件驗證、重設密碼
- **個人中心**：個人資料、我的文章、互動紀錄

### 管理後台（Admin）

- **內容管理**：文章編輯、發布、草稿管理
- **標籤管理**：標籤建立與階層關係管理
- **使用者管理**：使用者資訊與權限控管
- **資料分析**：造訪統計、內容表現分析
- **系統設定**：網站設定與主題設定

## 🌐 部署

### 開發環境

支援熱重載開發，前後端可獨立啟動，方便除錯。

### 正式環境

- **前端**：建置工具使用 Vite，支援程式碼分割與快取最佳化
- **後端**：Spring Boot 打包為 JAR，支援 Docker 容器化部署
- **資料庫**：MySQL 主從架構，Redis 叢集快取
- **負載平衡**：Nginx 反向代理，支援 HTTPS

## 📈 效能最佳化

### 前端最佳化成果

- **封裝體積**：主檔案由 1,079KB 降至 415KB（下降 61.6%）
- **程式碼分割**：vendor chunk 分離，利於長期快取
- **延遲載入**：路由層級按需載入
- **壓縮最佳化**：Terser 壓縮，移除除錯程式碼

### 後端最佳化

- **連線池**：HikariCP 資料庫連線最佳化
- **快取策略**：Redis 多層快取
- **非阻塞處理**：Spring `@Async`
- **JVM 調校**：記憶體與 GC 最佳化，提升正式環境效能

## 🔗 相關連結

- **線上體驗**：[https://stanli.site/stan-blog](https://stanli.site/stan-blog)
- **API 文件**： [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- **專案看板**： [GitLab Projects](https://stanli.site:8888/stanli/stan-blog)
- **問題回報**： [GitLab Issues](https://stanli.site:8888/stanli/stan-blog/-/issues)
