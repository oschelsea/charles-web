# Charles Web 项目指南

前后端分离的管理系统，扩展了地理信息管理模块（xenon-module-geo）。

## 架构

```
xenon/
├── xenon-common/              # 平台基础层（工具类）
│   ├── xenon-common-core/
│   └── xenon-common-web/
├── xenon-system/       # 系统管理模块（扁平结构）
├── xenon-geo/          # 地理信息模块（父子结构）
│   ├── xenon-geo-core/    # 核心实体/Mapper
│   ├── xenon-geo-tools/   # GeoTools 集成
│   ├── xenon-geo-ows/     # OGC 服务（WMS/WFS/WMTS）
│   ├── xenon-geo-rest/    # REST API
│   ├── xenon-geo-3d/      # 3D Tiles
│   └── xenon-geo-tile/    # 瓦片服务
└── xenon-admin/               # Spring Boot 应用入口
```

**技术栈**：Spring Boot 3.5.9 + MyBatis-Plus + Spring Security + JWT，数据库支持 SQLite/PostgreSQL。

## 构建和运行

```bash
# 后端开发运行
mvn spring-boot:run -pl xenon-admin

# 后端打包（跳过测试）
mvn clean package -DskipTests

# 前端开发（manager-ui-vue3/）
pnpm dev

# 前端构建
pnpm build
```

**数据库配置**：`xenon-admin/src/main/resources/application.yml`，默认 SQLite，通过 `app.database.type` 切换 PostgreSQL。

## 代码约定

### 后端
- 实体类继承 `BaseEntity`，使用 MyBatis-Plus 注解
- Controller 在各业务模块的 `controller` 包下
- Service 层处理业务逻辑，Mapper 继承 `BaseMapper<T>`
- GEO 模块遵循分层：geo-core（实体）→ geotools（数据处理）→ ows/rest（服务层）

### 前端（manager-ui-vue3/）
- 使用 Vue 3 Composition API + TypeScript
- UI 框架：Naive UI
- 状态管理：Pinia
- API 请求：`src/service/request/` 封装的 axios 实例
- 路由：`src/router/routes/`，使用 @elegant-router 自动生成

### GEO 模块 API 路径
- REST 管理 API：`/api/geo/*`（工作空间、数据存储、图层、样式管理）
- OGC 服务：`/ows/*`（WMS/WFS/WMTS 标准接口）
- 瓦片服务：`/tiles/{layer}/{z}/{x}/{y}.{format}`

## 模块命名规范

- 平台模块：`xenon-common-*`（如 `xenon-common-core`）
- 业务模块：`xenon-{业务名}`（如 `xenon-system`、`xenon-geo`）
- 业务子模块：`xenon-{业务名}-{功能}`（如 `xenon-geo-core`、`xenon-geo-ows`）

新增业务模块在根目录下创建，遵循 `xenon-{业务名}` 命名。
