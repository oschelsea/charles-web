## 1. 后端基础设施

- [x] 1.1 创建 `HttpStatusCodeException` 通用异常类（xenon-common-core）
- [x] 1.2 创建 `ErrorResponse` 统一错误响应类（xenon-common-core）
- [x] 1.3 修改 `ServletUtils.renderString()` 支持自定义状态码参数
- [x] 1.4 删除旧的 `GlobalExceptionHandler`（xenon-common-web）

## 2. 统一全局异常处理器

- [x] 2.1 将 `XenonGlobalExceptionHandler` 移动到 xenon-common-web
- [x] 2.2 添加 `AuthenticationException` 处理（返回 401）
- [x] 2.3 添加 `AccessDeniedException` 处理（返回 403）
- [x] 2.4 添加 `ServiceException` 处理（返回 400）
- [x] 2.5 添加 `HttpStatusCodeException` 处理（返回指定状态码）
- [x] 2.6 添加通用 `Exception` 处理（返回 500）
- [x] 2.7 配置 `@RestControllerAdvice` 扫描所有 Controller 包

## 3. 安全相关响应处理

- [x] 3.1 修改 `AuthenticationEntryPointImpl` 使用 `ResponseEntity<ErrorResponse>`
- [x] 3.2 修改 `LogoutSuccessHandlerImpl` 保持 200 状态码
- [x] 3.3 修改 `RepeatSubmitInterceptor` 返回 429 Too Many Requests
- [x] 3.4 检查其他安全相关 Handler，确保正确使用状态码

## 4. 前端响应处理

- [x] 4.1 修改 axios 响应拦截器，根据 HTTP 状态码判断成功/失败
- [x] 4.2 处理 401 状态码，跳转登录页面
- [x] 4.3 处理 403 状态码，显示权限不足提示
- [x] 4.4 处理其他错误状态码，显示错误消息
- [x] 4.5 更新相关业务代码，移除对 `response.data.code` 的判断

## 4.5 兼容 R.fail() 响应处理

- [x] 4.5.1 创建 `ResponseBodyHandler` 拦截 `R<T>` 返回值
- [x] 4.5.2 根据 `R.code` 自动设置正确的 HTTP 状态码

## 5. 测试与验证

- [x] 5.1 编写后端单元测试验证异常处理器行为
- [x] 5.2 测试认证失败场景（401）
- [x] 5.3 测试权限不足场景（403）
- [x] 5.4 测试资源不存在场景（404）
- [x] 5.5 测试参数校验失败场景（400）
- [x] 5.6 测试业务异常场景（400）
- [x] 5.7 测试系统异常场景（500）
- [ ] 5.8 前端集成测试验证错误处理流程
- [ ] 5.9 运行安全扫描工具验证误报消除
