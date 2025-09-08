# Stan blog 前端项目

[![code style: prettier](https://img.shields.io/badge/code_style-prettier-ff69b4.svg?style=flat-square)](https://github.com/prettier/prettier)
[![commitment](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg?style=flat-square)](http://commitizen.github.io/cz-cli/)
[![PRs](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)]()

## 环境

```json
{
  "node": "18.20.2",
  "npm": "10.5.0"
}
```

## 前置

- Git
- NodeJS
- NPM

## 安装依赖

```bash
npm i
```

## 自定义

重命名 `customization-example.json` 为 `customization.json`。编辑文件来自定义博客。

## 如何运行

```bash
npm run dev
```

## 如何构建

### 构建生产版本

```bash
npm run build
```

### 使用 Docker 构建

```bash
docker build -t stan_blog_web .
```
