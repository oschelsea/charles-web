package com.xenon.admin.security.handle;

import com.xenon.admin.manager.AsyncManager;
import com.xenon.admin.manager.factory.AsyncFactory;
import com.xenon.admin.service.TokenService;
import com.xenon.common.constant.Constants;
import com.xenon.common.core.domain.R;
import com.xenon.common.utils.JsonUtil;
import com.xenon.common.utils.ServletUtils;
import com.xenon.common.utils.StringUtils;
import com.xenon.framework.security.LoginUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

/**
 * 自定义退出处理类 返回成功
 *
 * @author charles
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;

    /**
     * 退出处理
     *
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, "退出成功"));
        }
        ServletUtils.renderString(response, JsonUtil.toJson(R.fail(HttpStatus.OK.value(), "退出成功")));
    }
}
