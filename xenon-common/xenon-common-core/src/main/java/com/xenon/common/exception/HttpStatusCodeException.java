package com.xenon.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 通用 HTTP 状态码异常
 * 用于业务代码抛出指定 HTTP 状态码的错误
 *
 * @author charles
 */
public class HttpStatusCodeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码
     */
    private final int statusCode;

    /**
     * 业务错误码（可选）
     */
    private Integer code;

    /**
     * 错误类型（如 "Bad Request"）
     */
    private final String error;

    public HttpStatusCodeException(HttpStatus status) {
        super(status.getReasonPhrase());
        this.statusCode = status.value();
        this.error = status.getReasonPhrase();
    }

    public HttpStatusCodeException(HttpStatus status, String message) {
        super(message);
        this.statusCode = status.value();
        this.error = status.getReasonPhrase();
    }

    public HttpStatusCodeException(HttpStatus status, String message, Integer code) {
        super(message);
        this.statusCode = status.value();
        this.error = status.getReasonPhrase();
        this.code = code;
    }

    public HttpStatusCodeException(int statusCode, String error, String message) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
    }

    public HttpStatusCodeException(int statusCode, String error, String message, Integer code) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
        this.code = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 创建 400 Bad Request 异常
     */
    public static HttpStatusCodeException badRequest(String message) {
        return new HttpStatusCodeException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 创建 400 Bad Request 异常（带业务错误码）
     */
    public static HttpStatusCodeException badRequest(String message, Integer code) {
        return new HttpStatusCodeException(HttpStatus.BAD_REQUEST, message, code);
    }

    /**
     * 创建 404 Not Found 异常
     */
    public static HttpStatusCodeException notFound(String message) {
        return new HttpStatusCodeException(HttpStatus.NOT_FOUND, message);
    }

    /**
     * 创建 409 Conflict 异常
     */
    public static HttpStatusCodeException conflict(String message) {
        return new HttpStatusCodeException(HttpStatus.CONFLICT, message);
    }

    /**
     * 创建 500 Internal Server Error 异常
     */
    public static HttpStatusCodeException internalServerError(String message) {
        return new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
