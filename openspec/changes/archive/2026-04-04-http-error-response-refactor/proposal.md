## Why

当前项目所有 API 响应都返回 HTTP 200 状态码，即使在认证失败、权限不足、资源不存在等异常情况下也是如此。这导致安全扫描软件产生大量误报，认为接口存在漏洞（如未授权访问）。需要全面切换到 RESTful 标准的 HTTP 状态码响应方式，消除安全隐患并提升 API 语义正确性。

## What Changes

- **BREAKING** 统一使用正确的 HTTP 状态码（>=400 表示错误）
- **BREAKING** 统一响应结构为 `ResponseEntity<ErrorResponse>`
- 重构 `GlobalExceptionHandler` 和 `XenonGlobalExceptionHandler` 合并为统一的全局异常处理器
- 新增通用异常类 `HttpStatusCodeException` 用于业务代码抛出标准 HTTP 错误
- 修改 `ServletUtils.renderString()` 支持自定义状态码
- 重构安全相关的响应处理（`AuthenticationEntryPointImpl`、`LogoutSuccessHandlerImpl` 等）
- **BREAKING** 前端响应拦截器需要同步修改，正确处理 HTTP 错误状态码

## Capabilities

### New Capabilities

- `error-response`: 统一错误响应处理机制，包含 HTTP 状态码映射、异常分类处理、响应格式标准化

### Modified Capabilities

- 无（当前没有正式定义的 specs）

## Impact

**后端影响**：
- `xenon-common-core`: 修改 `ServletUtils.renderString()`，新增 `HttpStatusCodeException` 异常类
- `xenon-common-web`: 重构 `GlobalExceptionHandler`，可能删除或合并到统一处理器
- `xenon-geo-rest`: 扩展 `XenonGlobalExceptionHandler` 作为统一的全局异常处理器
- `xenon-admin`: 修改 `AuthenticationEntryPointImpl`、`LogoutSuccessHandlerImpl`、`RepeatSubmitInterceptor`

**前端影响**：
- `manager-ui-vue3/src/service/request/`: 修改 axios 响应拦截器
- 前端需要从 HTTP 状态码判断请求成功/失败，而非仅依赖响应体中的 `code` 字段

**兼容性**：
- 这是一个 **Breaking Change**，前后端需要同步发布
- 建议在低峰期发布，或提供过渡期支持
