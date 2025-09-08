## 準備

- JDK 17
- Maven
- IDE（Cursor、VSCode、IntellJ）

## 配置

### Dev

建立 `application-dev.yml` 檔案，並基於 `src/main/resources/application-example.yml` 進行設定。

### Production

建立 `application-prod.yml` 檔案，並基於 `src/main/resources/application-example.yml` 進行設定。

### Test

建立 `application-test.yml` 檔案，並基於 `src/test/resources/application-example.yml` 進行設定。

## 啟動

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

預設埠為 `8080`

## 打包

本地或測試環境

```bash
mvn package -Dspring-boot.run.profiles=dev
```

生產環境

```bash
mvn package -Dspring-boot.run.profiles=prod
```

使用 Docker:

```bash
docker build -t stan-blog-service .
```

## 測試

本專案透過單元測試來保證程式碼品質，利用 `Juint5 + H2` 的方案實現從功能介面、業務實作，再到資料庫資料的全面覆蓋。

```bash
mvn test -Dspring-boot.run.profiles=test
```

## API

專案遵循 RESTful API 設計原則並已整合 spring-doc 外掛，因此可透過 `/swagger-ui/index.html` 頁面查看並調試所有 API。

## 依賴

主要依賴如下：

- spring-boot: 3.2.5
- spring-security: 6.2.4
- mybatis-plus: 3.5.6
- spring-boot-starter-data-redis: 3.2.5
- spring-doc: 2.5.0
- h2: 2.2.224
- Junit: 5.9.2
