package io.charles.project.common;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.core.util.IdUtil;
import io.charles.common.constant.Constants;
import io.charles.common.enums.CaptchaType;
import io.charles.common.utils.StringUtils;
import io.charles.framework.cache.ICacheService;
import io.charles.framework.config.properties.CaptchaProperties;
import io.charles.framework.web.domain.R;
import io.charles.project.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
public class CaptchaController {
    private final AbstractCaptcha captchaGenerator;
    private final CaptchaProperties captchaProperties;
    private final ICacheService cacheCache;
    private final ISysConfigService configService;
    private final ExpressionParser parser = new SpelExpressionParser();

    public record CaptchaVo(boolean captchaOnOff, String uuid, String img) {
    }

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public R<CaptchaVo> getCode(HttpServletResponse response) throws IOException {
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        if (!captchaOnOff) {
            return R.ok(new CaptchaVo(false, null, null));
        }
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
        captchaGenerator.createCode();

        String code = captchaGenerator.getCode();
        // 如果是数学验证码，使用SpEL表达式处理验证码结果
        if (captchaProperties.getType().equals(CaptchaType.MATH)) {
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        cacheCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION.longValue(), TimeUnit.MINUTES);
        return R.ok(new CaptchaVo(true, uuid, captchaGenerator.getImageBase64()));
    }
}
