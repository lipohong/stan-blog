# Stan blog front end

[![code style: prettier](https://img.shields.io/badge/code_style-prettier-ff69b4.svg?style=flat-square)](https://github.com/prettier/prettier)
[![commitment](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg?style=flat-square)](http://commitizen.github.io/cz-cli/)
[![PRs](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)]()

[中文文檔](doc/README_TC.md) | [中文文档](doc/README_SC.md)

## Environment

```json
{
  "node": "18.20.2",
  "npm": "10.5.0"
}
```

## Prerequisites

- Git
- NodeJS
- NPM

## Install dependencies

```bash
npm i
```

## Customization

Rename `customization-example.json` to `customization.json`. Edit the file to customize the blog.

## How to run

```bash
npm run dev
```

## How to build

### Build for production

```bash
npm run build
```

### Build using Docker

```bash
docker build -t stan_blog_web .
```
