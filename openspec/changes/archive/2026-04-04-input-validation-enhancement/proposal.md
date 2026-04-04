## Why

当前项目输入校验存在以下问题：
1. **校验覆盖不完整**：部分 Controller 方法缺少 `@Validated` 注解，路径参数缺少校验
2. **缺少自定义校验注解**：手机号、身份证等业务常用字段需要重复编写正则表达式
3. **方法级校验异常未处理**：`ConstraintViolationException` 未在全局异常处理器中处理

这些问题可能导致无效数据进入业务层，增加安全风险和调试成本。

## What Changes

### 1. 完善现有校验
- 补充 Controller 方法缺失的 `@Validated` 注解
- 添加路径参数校验（`@PathVariable` + `@Min`/`@Max`/`@Pattern` 等）
- 在 GlobalExceptionHandler 中添加 `ConstraintViolationException` 处理

### 2. 自定义校验注解
新增以下自定义校验注解：
- `@Mobile` - 中国大陆手机号校验
- `@IdCard` - 中国大陆身份证号校验（支持15位和18位）
- `@EnumValue` - 枚举值校验（检查值是否在指定枚举范围内）

## Capabilities

### New Capabilities
- `input-validation`: 输入校验能力，包含标准注解使用、自定义校验注解、异常处理

### Modified Capabilities
- `error-response`: 扩展错误响应结构支持 `ConstraintViolationException` 的字段级错误详情

## Impact

**代码影响**：
- `xenon-common-core`: 新增自定义校验注解和校验器
- `xenon-common-web`: 扩展 GlobalExceptionHandler
- `xenon-system`: 更新 SysUser 等实体使用新的校验注解
- `xenon-admin`: 补充 Controller 校验注解

**API 影响**：
- 校验失败返回 400 状态码，响应体包含详细的字段错误信息
- 无破坏性变更，仅增强校验逻辑
