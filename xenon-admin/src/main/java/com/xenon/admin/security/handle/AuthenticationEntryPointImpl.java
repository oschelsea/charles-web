package com.xenon.admin.security.handle;

import com.xenon.common.core.domain.ErrorResponse;
import com.xenon.common.utils.JsonUtil;
import com.xenon.common.utils.ServletUtils;
import com.xenon.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

/**
 * 认证失败处理类 返回未授权
 *
 * @author charles
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(msg)
                .code(HttpStatus.UNAUTHORIZED.value())
                .build();

        ServletUtils.renderString(response, HttpStatus.UNAUTHORIZED.value(), JsonUtil.toJson(error));
    }
}
