## Context

项目已使用 Spring Boot Validation (hibernate-validator)，在 GEO 模块和系统模块有部分使用。需要统一完善校验机制并添加业务常用校验注解。

**当前状态**：
- 已有 `@NotBlank`、`@Size`、`@Email`、`@Pattern` 等标准注解使用
- GlobalExceptionHandler 已处理 `BindException` 和 `MethodArgumentNotValidException`
- 缺少 `ConstraintViolationException` 处理（方法级校验）

## Goals / Non-Goals

**Goals:**
- 完善现有 Controller 校验覆盖
- 添加 `ConstraintViolationException` 异常处理
- 创建 3 个自定义校验注解：`@Mobile`、`@IdCard`、`@EnumValue`
- 统一校验失败的响应格式

**Non-Goals:**
- 不实现校验分组（Create/Update 分组）- 可后续扩展
- 不实现跨字段校验（如密码确认）- 复杂度较高
- 不修改前端校验逻辑

## Decisions

### 1. 自定义校验注解位置

**决策**: 放在 `xenon-common-core` 的 `com.xenon.common.validation` 包下

**理由**:
- 校验注解是通用能力，应放在 common 层
- 与现有 `exception`、`utils` 包平级，便于查找

### 2. 校验注解设计

```
┌─────────────────────────────────────────────────────────────┐
│                    自定义校验注解结构                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  @Mobile                                                    │
│  ├── message: "手机号格式不正确" (默认消息)                  │
│  ├── groups: {} (校验分组)                                  │
│  └── payload: {} (元数据)                                   │
│                                                             │
│  @IdCard                                                    │
│  ├── message: "身份证号格式不正确"                           │
│  ├── groups: {}                                             │
│  └── payload: {}                                            │
│                                                             │
│  @EnumValue                                                 │
│  ├── enumClass: Class<? extends Enum<?>> (必填)             │
│  ├── message: "值必须在枚举范围内"                           │
│  ├── groups: {}                                             │
│  └── payload: {}                                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3. 异常处理增强

**新增 `ConstraintViolationException` 处理**：

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorResponse> handleConstraintViolation(
        ConstraintViolationException e, HttpServletRequest request) {
    // 提取字段名和错误消息
    Map<String, String> details = new HashMap<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
        String propertyPath = violation.getPropertyPath().toString();
        // 提取最后一个属性名（去掉方法名前缀）
        String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
        details.put(fieldName, violation.getMessage());
    }
    return ResponseEntity.badRequest()
            .body(ErrorResponse.badRequest("参数校验失败", details));
}
```

### 4. Controller 校验补充

**需要添加 @Validated 的场景**：
- `SysUserController.resetPwd` - 密码重置
- `SysUserController.changeStatus` - 状态修改

**路径参数校验示例**：
```java
@GetMapping("/{id}")
public R<User> getUser(@PathVariable @Min(1) Long id) { ... }
```

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 自定义注解正则可能不全面 | 使用成熟的开源正则表达式，添加单元测试覆盖边界情况 |
| 校验失败响应格式变化 | 已有 ErrorResponse 结构，新增 details 字段复用 |
| 性能影响（正则校验） | 校验在控制器层执行，性能影响可忽略 |

## Migration Plan

1. **阶段一**：添加 `ConstraintViolationException` 异常处理（无破坏性）
2. **阶段二**：创建自定义校验注解和校验器
3. **阶段三**：更新现有实体类使用新注解（替换手动正则）
4. **阶段四**：补充 Controller 校验注解

无需回滚策略，所有变更都是增量式的。
