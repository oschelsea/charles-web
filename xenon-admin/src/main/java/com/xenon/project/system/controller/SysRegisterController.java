package com.xenon.project.system.controller;

import com.xenon.admin.service.SysRegisterService;
import com.xenon.common.core.domain.R;
import com.xenon.framework.security.RegisterBody;
import com.xenon.framework.web.controller.BaseController;
import com.xenon.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
public class SysRegisterController extends BaseController {
    private final SysRegisterService registerService;
    private final ISysConfigService configService;

    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return R.fail("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? R.ok() : R.fail(msg);
    }
}
