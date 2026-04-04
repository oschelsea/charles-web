package com.xenon.framework.web.exception;

import com.xenon.common.core.domain.ErrorResponse;
import com.xenon.common.exception.*;
import com.xenon.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一全局异常处理器
 * 所有异常返回正确的 HTTP 状态码
 *
 * @author charles
 */
@Slf4j
@RestControllerAdvice(basePackages = {
        "com.xenon.project",    // xenon-admin controllers
        "com.xenon.rest"        // xenon-geo-rest controllers
})
public class GlobalExceptionHandler {

    // ==================== 安全相关异常 ====================

    /**
     * 认证失败异常 - 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.unauthorized("认证失败，请重新登录"));
    }

    /**
     * 权限不足异常 - 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.forbidden("没有权限，请联系管理员授权"));
    }

    // ==================== 资源相关异常 ====================

    /**
     * 资源不存在异常 - 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("资源不存在: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.notFound(e.getMessage()));
    }

    /**
     * 资源已存在异常 - 409
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException e, HttpServletRequest request) {
        log.warn("资源已存在: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conflict(e.getMessage()));
    }

    // ==================== 参数校验异常 ====================

    /**
     * 参数绑定异常 - 400
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            org.springframework.validation.BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, String> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));

        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? "参数绑定失败"
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.badRequest(message, details));
    }

    /**
     * 参数校验异常 - 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验失败: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, String> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));

        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? "参数校验失败"
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.badRequest(message, details));
    }

    /**
     * 参数类型不匹配异常 - 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.badRequest(String.format("参数 '%s' 类型不正确", e.getName())));
    }

    // ==================== 业务异常 ====================

    /**
     * 业务异常 - 400
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(
            ServiceException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());

        Integer code = StringUtils.isNotNull(e.getCode()) ? e.getCode() : HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), code));
    }

    /**
     * 演示模式异常 - 403
     */
    @ExceptionHandler(DemoModeException.class)
    public ResponseEntity<ErrorResponse> handleDemoModeException(
            DemoModeException e, HttpServletRequest request) {
        log.warn("演示模式限制: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.forbidden("演示模式，不允许操作"));
    }

    // ==================== 通用 HTTP 状态码异常 ====================

    /**
     * 通用 HTTP 状态码异常 - 使用异常指定的状态码
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleHttpStatusCodeException(
            HttpStatusCodeException e, HttpServletRequest request) {
        log.warn("HTTP 异常 ({}): {} - {}", e.getStatusCode(), request.getRequestURI(), e.getMessage());

        ErrorResponse error = e.getCode() != null
                ? ErrorResponse.of(HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e.getCode())
                : ErrorResponse.of(HttpStatus.valueOf(e.getStatusCode()), e.getMessage());

        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    // ==================== 请求方法异常 ====================

    /**
     * 请求方法不支持异常 - 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: {} - {}", request.getRequestURI(), e.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED,
                        String.format("不支持 '%s' 请求方法", e.getMethod())));
    }

    // ==================== 未知异常 ====================

    /**
     * 用户相关异常（不记录日志）- 400
     */
    @ExceptionHandler(com.xenon.common.exception.user.UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(
            com.xenon.common.exception.user.UserException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.badRequest(e.getMessage()));
    }

    /**
     * 未知运行时异常 - 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {
        log.error("请求地址'{}',发生未知异常.", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.internalServerError("系统繁忙，请稍后再试"));
    }

    /**
     * 未知异常 - 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("请求地址'{}',发生系统异常.", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.internalServerError("系统繁忙，请稍后再试"));
    }
}
