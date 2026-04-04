## 1. 异常处理增强

- [x] 1.1 在 GlobalExceptionHandler 中添加 `ConstraintViolationException` 处理
- [x] 1.2 提取约束违反中的字段名和错误消息
- [x] 1.3 返回 400 状态码和详细的字段错误信息

## 2. 自定义校验注解 - @Mobile

- [x] 2.1 创建 `@Mobile` 注解（com.xenon.common.validation）
- [x] 2.2 创建 `MobileValidator` 校验器
- [x] 2.3 实现中国大陆手机号正则校验（以1开头的11位数字）
- [x] 2.4 编写单元测试验证校验器行为

## 3. 自定义校验注解 - @IdCard

- [x] 3.1 创建 `@IdCard` 注解
- [x] 3.2 创建 `IdCardValidator` 校验器
- [x] 3.3 实现15位和18位身份证号校验（含校验位验证）
- [x] 3.4 编写单元测试验证校验器行为

## 4. 自定义校验注解 - @EnumValue

- [x] 4.1 创建 `@EnumValue` 注解（含 `enumClass` 属性）
- [x] 4.2 创建 `EnumValueValidator` 校验器
- [x] 4.3 实现枚举值存在性检查
- [x] 4.4 编写单元测试验证校验器行为

## 5. Controller 校验补充

- [x] 5.1 检查所有 Controller 方法，补充缺失的 `@Validated` 注解
- [x] 5.2 为 `SysUserController.resetPwd` 添加 `@Validated`
- [x] 5.3 为 `SysUserController.changeStatus` 添加 `@Validated`
- [x] 5.4 检查其他需要补充校验的 Controller 方法

## 6. 实体类校验更新

- [x] 6.1 更新 `SysUser.phonenumber` 使用 `@Mobile` 注解
- [x] 6.2 检查其他实体类是否有可替换的手动校验逻辑

## 7. 测试与验证

- [x] 7.1 测试 `@Mobile` 校验（有效/无效/null 值）
- [x] 7.2 测试 `@IdCard` 校验（15位/18位/无效值）
- [x] 7.3 测试 `@EnumValue` 校验（有效/无效值）
- [x] 7.4 测试 `ConstraintViolationException` 异常处理
- [x] 7.5 验证校验失败返回正确的 HTTP 400 和错误详情
