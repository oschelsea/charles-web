## Context

当前项目存在两套异常处理机制：
1. `GlobalExceptionHandler`（xenon-common-web）：部分使用 `@ResponseStatus`，部分未使用
2. `XenonGlobalExceptionHandler`（xenon-geo-rest）：正确使用 `ResponseEntity` 返回状态码

核心问题在于 `ServletUtils.renderString()` 硬编码 `response.setStatus(200)`，导致所有通过该方法返回的响应都是 200 状态码。

```
当前请求流程：
┌─────────┐    ┌──────────────────┐    ┌─────────────────────┐
│ 请求    │───▶│ ExceptionHandler │───▶│ ServletUtils        │
│         │    │ 或 Security      │    │ .renderString()     │
│         │    │ Handler          │    │ setStatus(200) ◀── 问题根源
│         │    │                  │    │                     │
└─────────┘    └──────────────────┘    └─────────────────────┘
```

## Goals / Non-Goals

**Goals:**
- 所有错误响应返回正确的 HTTP 状态码（>=400）
- 统一使用 `ResponseEntity<ErrorResponse>` 作为响应结构
- 合并两套异常处理器为统一的全局异常处理器
- 前端正确处理 HTTP 错误状态码
- 保持响应体中包含业务错误码，便于前端精细化处理

**Non-Goals:**
- 不改变现有的业务逻辑和错误类型
- 不修改数据库结构
- 不引入新的依赖库

## Decisions

### 决策 1：统一异常处理器位置

**选择**：将 `XenonGlobalExceptionHandler` 移动到 `xenon-common-web`，作为统一的全局异常处理器

**原因**：
- `xenon-common-web` 是平台基础层，被所有模块依赖
- 删除旧的 `GlobalExceptionHandler`，避免重复处理
- 保留 `ErrorResponse` 结构，已在 geo 模块验证

**替代方案**：
- 方案 B：保留两个处理器，通过 `@RestControllerAdvice` 的 basePackages 区分
- 放弃原因：增加维护成本，容易产生不一致

### 决策 2：HTTP 状态码映射规则

**选择**：按以下规则映射异常到 HTTP 状态码

| 异常类型 | HTTP 状态码 | 说明 |
|---------|------------|------|
| `AuthenticationException` | 401 | 认证失败 |
| `AccessDeniedException` | 403 | 权限不足 |
| `ResourceNotFoundException` | 404 | 资源不存在 |
| `MethodArgumentNotValidException` | 400 | 参数校验失败 |
| `BindException` | 400 | 参数绑定失败 |
| `ServiceException` | 400 | 业务逻辑错误 |
| `HttpStatusCodeException` | 由异常指定 | 通用 HTTP 错误 |
| `Exception` | 500 | 未知系统错误 |

**原因**：
- 业务逻辑错误使用 400 是业界常见做法
- 避免使用 422（Unprocessable Entity），简化前端处理

### 决策 3：响应结构

**选择**：使用 `ErrorResponse` 统一响应结构

```java
public class ErrorResponse {
    private String timestamp;
    private int status;       // HTTP 状态码
    private String error;     // 错误类型（如 "Bad Request"）
    private String message;   // 用户友好的错误信息
    private Integer code;     // 业务错误码（可选）
    private Map<String, String> details; // 详细错误信息（可选）
}
```

**原因**：
- 保持与现有 `R<T>` 结构类似的 `code` 字段，便于前端迁移
- `message` 字段始终返回用户友好的错误信息
- `details` 用于验证错误等需要返回多个错误的场景

### 决策 4：前端响应处理

**选择**：修改 axios 响应拦截器，根据 HTTP 状态码判断成功/失败

```javascript
// 响应拦截器
axios.interceptors.response.use(
  response => {
    // 2xx 状态码视为成功
    return response.data;
  },
  error => {
    // 4xx/5xx 状态码进入错误处理
    const { response } = error;
    if (response) {
      // 统一从响应体获取错误信息
      const { message, code } = response.data;
      // 处理特定状态码
      if (response.status === 401) {
        // 跳转登录
      }
    }
    return Promise.reject(error);
  }
);
```

**原因**：
- 符合 axios 标准用法
- 减少前端代码复杂度
- 利用浏览器和 axios 的标准错误处理机制

## Risks / Trade-offs

### 风险 1：前端兼容性

**风险**：前端现有代码依赖 `response.data.code` 判断成功/失败，修改后可能遗漏处理

**缓解措施**：
- 前后端同步发布
- 发布前进行全面的集成测试
- 提供过渡期：响应体中仍保留 `code` 字段

### 风险 2：第三方集成

**风险**：如果有外部系统调用 API，可能依赖当前的 200 响应格式

**缓解措施**：
- 检查 API 调用日志，确认是否有外部调用
- 如有外部调用，提前通知相关方

### 风险 3：监控和日志

**风险**：现有监控可能依赖 200 状态码统计成功率

**缓解措施**：
- 检查监控配置，更新告警规则
- 新的监控应该区分 4xx（客户端错误）和 5xx（服务端错误）
