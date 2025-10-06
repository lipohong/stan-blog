# Stan-blog

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.0-blue.svg)](https://reactjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2.2-blue.svg)](https://www.typescriptlang.org)

A modern open-source blog platform based on Spring Boot and React, supporting multiple languages, multi-device adaptation, and providing comprehensive content management and data analytics features.

English | [繁體中文](README_TC.md) | [简体中文](README_SC.md)

## ✨ Key Features

### 🎨 User Interface

- **Modern Design**: Built with Material-UI design system, supporting dark/light theme switching
- **Responsive Layout**: Perfectly adapted for desktop, tablet, and mobile devices
- **Multi-language Support**: Internationalization solution based on i18next
- **Accessibility**: Compliant with WCAG standards, supporting screen readers

### 📝 Content Management

- **Rich Text Editor**: Integrated WangEditor supporting multimedia content like images, videos, and code blocks
- **Smart Tag System**: Supporting tag hierarchical relationships to build complex content classification systems
- **Diverse Content Types**: Supporting articles, plans, vocabularies, and other content types
- **Auto-save Drafts**: Preventing accidental loss of creative content

### 🔐 User System

- **JWT Authentication**: Secure user authentication and authorization mechanism
- **Email Verification**: Complete user registration and password reset workflow
- **Personal Center**: User profile management and preference settings
- **Multi-device Sync**: Cross-device data synchronization

### 📊 Data Analytics

- **Content Statistics**: Real-time statistics for article views, likes, etc.
- **User Behavior**: Detailed user access and interaction data
- **Visual Charts**: Intuitive data presentation and trend analysis
- **Export Functionality**: Support for data export and report generation

### 🚀 Performance Optimization

- **Code Splitting**: Route-level lazy loading, 60%+ improvement in first-screen loading speed
- **Caching Strategy**: Smart frontend caching and CDN acceleration
- **SEO Friendly**: Server-side rendering support, search engine optimization
- **PWA Support**: Offline access and push notifications

## 🛠️ Tech Stack

### Frontend (stan_blog_web)

- **Framework**: React 18.3.0 + TypeScript 5.2.2
- **Build Tool**: Vite 5.2.0 (supporting hot reload and fast build)
- **UI Component Library**: Material-UI 5.15.18
- **State Management**: React Context + Hooks
- **Routing**: React Router DOM 6.23.1
- **HTTP Client**: Axios 0.28.0
- **Rich Text Editor**: WangEditor 5.1.20
- **Internationalization**: i18next 21.8.10
- **Date Processing**: Moment.js 2.29.4
- **Animation**: React Spring 9.7.1
- **Code Quality**: ESLint + Prettier + TypeScript

### Backend (stan_blog_service)

- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6.2.4 + JWT
- **Database**: MySQL + MyBatis Plus 3.5.6
- **Cache**: Redis (Spring Data Redis)
- **Documentation**: SpringDoc OpenAPI 2.5.0 (Swagger UI)
- **Email**: Spring Boot Starter Mail + Thymeleaf
- **Monitoring**: Spring Boot Actuator
- **Testing**: JUnit 5.9.2 + H2 Database
- **Tools**: Lombok 1.18.38 + Apache Commons

### Development Environment

- **JDK**: 17
- **Node.js**: 18.20.2 (managed with Volta)
- **Package Management**: Maven + NPM
- **Version Control**: Git
- **IDE**: Supporting Cursor, VSCode, IntelliJ IDEA

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Node.js 18.20.2+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### Backend Service Startup

```bash
# Navigate to backend project directory
cd stan_blog_service

# Install dependencies and start (development environment)
mvn spring-boot:run

# Or build for production environment
mvn package -Pprod
```

Default port: `8080`  
API Documentation: `http://localhost:8080/swagger-ui/index.html`

### Frontend Application Startup

```bash
# Navigate to frontend project directory
cd stan_blog_web

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production environment
npm run build
```

Default port: `3000`  
Access URL: `http://localhost:3000`

## 📖 Project Structure

```
stan-blog/
├── stan_blog_service/          # Backend service
│   ├── src/main/java/com/stan/blog/
│   │   ├── core/              # Core functionality modules
│   │   ├── portal/            # Portal APIs
│   │   ├── content/           # Content management
│   │   ├── analytics/         # Data analytics
│   │   └── beans/             # Data entities
│   ├── src/main/resources/    # Configuration files
│   └── src/test/             # Unit tests
│
├── stan_blog_web/             # Frontend application
│   ├── src/
│   │   ├── portal/           # User portal
│   │   ├── admin/            # Admin dashboard
│   │   ├── components/       # Common components
│   │   ├── services/         # API services
│   │   └── contexts/         # State management
│   ├── public/               # Static assets
│   └── deploy/               # Deployment related
│
├── docker_compose/           # Docker Compose files
├── Jenkinsfile               # Jenkins pipeline configuration
└── Jmeter/                   # JMeter test scripts and test user data
```

## 🧪 Testing

### Backend Testing

```bash
cd stan_blog_service
mvn test
```

The project adopts a layered testing strategy:

- **Unit Tests**: Covering service layer business logic
- **Integration Tests**: Testing API interfaces and database interactions
- **H2 In-memory Database**: Test environment isolation

### Frontend Testing

```bash
cd stan_blog_web
npm run lint          # Code style checking
npm run build:analyze  # Bundle analysis
```

### Load Testing

#### JMeter

```bash
# create folders
mkdir jmeter
mkdir jmeter\results
mkdir jmeter\logs

# Run test plan
jmeter.bat -n -t ".\jmeter\stanblog-test-plan.jmx" -l ".\jmeter\results.jtl" -j ".\jmeter\logs\jmeter.log"
```

## 📱 Feature Modules

### User Portal

- **Homepage Display**: Latest articles and popular content recommendations
- **Content Browsing**: Article details, tag classification, search functionality
- **User System**: Registration/login, email verification, password reset
- **Personal Center**: Personal profile, my articles, interaction records

### Admin Dashboard

- **Content Management**: Article editing, publishing, draft management
- **Tag Management**: Tag creation, hierarchical relationship management
- **User Management**: User information, permission control
- **Data Analytics**: Access statistics, content performance analysis
- **System Settings**: Website configuration, theme settings

## 🌐 Deployment

### Development Environment

The project supports hot reload development with independent frontend and backend startup for debugging.

### Production Environment

- **Frontend**: Built with Vite, supporting code splitting and cache optimization
- **Backend**: Spring Boot packaged as JAR, supporting Docker containerized deployment
- **Database**: MySQL master-slave architecture, Redis cluster caching
- **Load Balancing**: Nginx reverse proxy, supporting HTTPS

## 📈 Performance Optimization

### Frontend Optimization Results

- **Bundle Size**: Main file reduced from 1,079KB to 415KB (61.6% reduction)
- **Code Splitting**: Vendor chunk separation, long-term cache friendly
- **Lazy Loading**: Route-level on-demand loading
- **Compression Optimization**: Terser compression, removing debug code

### Backend Optimization

- **Connection Pool**: HikariCP database connection optimization
- **Caching Strategy**: Redis multi-level caching
- **Asynchronous Processing**: Spring @Async non-blocking operations
- **JVM Tuning**: Memory and GC optimization for production environment

## 🔗 Related Links

- **Live Demo**: [https://stan-blog.stanli.site](https://stan-blog.stanli.site)
- **API Documentation**: [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- **Project Board**: [Gitlab Projects](https://gitlab.stanli.site/stanli/stan-blog)
- **Issue Reporting**: [Gitlab Issues](https://gitlab.stanli.site/stanli/stan-blog/-/issues)
