# Stan blog 前端項目

[![code style: prettier](https://img.shields.io/badge/code_style-prettier-ff69b4.svg?style=flat-square)](https://github.com/prettier/prettier)
[![commitment](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg?style=flat-square)](http://commitizen.github.io/cz-cli/)
[![PRs](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)]()

## 環境

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

## 安裝依賴

```bash
npm i
```

## 自定義

重命名 `customization-example.json` 為 `customization.json`。編輯文件自定義博客。

## 如何運行

```bash
npm run dev
```

## 如何構建

### 構建生產版本

```bash
npm run build
```

### 使用 Docker 構建

```bash
docker build -t stan_blog_web .
```
