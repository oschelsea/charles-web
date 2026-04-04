package com.xenon.framework.web.advice;

import com.xenon.common.core.domain.R;
import com.xenon.common.constant.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体处理建议
 * 自动将 R.fail() 响应转换为正确的 HTTP 状态码
 *
 * @author charles
 */
@ControllerAdvice(basePackages = {
        "com.xenon.project",    // xenon-admin controllers
        "com.xenon.rest"        // xenon-geo-rest controllers
})
public class ResponseBodyHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理 R 类型的返回值
        return R.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof R<?> r && !R.isSuccess(r)) {
            // 根据 R 的 code 设置 HTTP 状态码
            int code = r.getCode();
            int httpStatus = mapToHttpStatus(code);

            // 设置 HTTP 响应状态码
            if (response instanceof ServletServerHttpResponse servletResponse) {
                HttpServletResponse httpResponse = servletResponse.getServletResponse();
                httpResponse.setStatus(httpStatus);
            }
        }

        return body;
    }

    /**
     * 将内部业务码映射为 HTTP 状态码
     */
    private int mapToHttpStatus(int code) {
        // 已经是有效的 HTTP 状态码
        if (code >= 400 && code < 600) {
            return code;
        }

        // 特殊映射
        return switch (code) {
            case HttpStatus.SUCCESS -> 200;
            case HttpStatus.UNAUTHORIZED -> 401;
            case HttpStatus.FORBIDDEN -> 403;
            case HttpStatus.NOT_FOUND -> 404;
            case HttpStatus.WARN -> 400; // 警告作为参数错误处理
            default -> 400; // 其他错误默认为 400
        };
    }
}
