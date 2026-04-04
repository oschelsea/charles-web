package com.xenon.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一错误响应结构
 *
 * @author charles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * HTTP 状态码
     */
    private int status;

    /**
     * 错误类型（如 "Bad Request"）
     */
    private String error;

    /**
     * 用户友好的错误信息
     */
    private String message;

    /**
     * 业务错误码（可选）
     */
    private Integer code;

    /**
     * 详细错误信息（如字段验证错误）
     */
    private Map<String, String> details;

    // ==================== 快捷工厂方法 ====================

    /**
     * 根据 HttpStatus 创建错误响应
     * 自动填充 timestamp、status、error、code
     *
     * @param status  HTTP 状态
     * @param message 错误消息
     * @return ErrorResponse
     */
    public static ErrorResponse of(HttpStatus status, String message) {
        return builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(status.value())
                .build();
    }

    /**
     * 根据 HttpStatus 创建错误响应（自定义业务码）
     *
     * @param status  HTTP 状态
     * @param message 错误消息
     * @param code    业务错误码
     * @return ErrorResponse
     */
    public static ErrorResponse of(HttpStatus status, String message, Integer code) {
        return builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .build();
    }

    /**
     * 根据 HttpStatus 创建错误响应（带详细信息）
     *
     * @param status  HTTP 状态
     * @param message 错误消息
     * @param details 详细错误信息
     * @return ErrorResponse
     */
    public static ErrorResponse of(HttpStatus status, String message, Map<String, String> details) {
        return builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(status.value())
                .details(details)
                .build();
    }

    // ==================== 常用 HTTP 状态快捷方法 ====================

    /**
     * 400 Bad Request
     */
    public static ErrorResponse badRequest(String message) {
        return of(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 400 Bad Request（带详细信息）
     */
    public static ErrorResponse badRequest(String message, Map<String, String> details) {
        return of(HttpStatus.BAD_REQUEST, message, details);
    }

    /**
     * 401 Unauthorized
     */
    public static ErrorResponse unauthorized(String message) {
        return of(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 403 Forbidden
     */
    public static ErrorResponse forbidden(String message) {
        return of(HttpStatus.FORBIDDEN, message);
    }

    /**
     * 404 Not Found
     */
    public static ErrorResponse notFound(String message) {
        return of(HttpStatus.NOT_FOUND, message);
    }

    /**
     * 409 Conflict
     */
    public static ErrorResponse conflict(String message) {
        return of(HttpStatus.CONFLICT, message);
    }

    /**
     * 429 Too Many Requests
     */
    public static ErrorResponse tooManyRequests(String message) {
        return of(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    /**
     * 500 Internal Server Error
     */
    public static ErrorResponse internalServerError(String message) {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
