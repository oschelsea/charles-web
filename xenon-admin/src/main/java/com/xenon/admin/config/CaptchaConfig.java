package com.xenon.admin.config;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.ReflectUtil;
import com.xenon.common.enums.CaptchaType;
import com.xenon.system.config.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.awt.*;

/**
 * 验证码配置
 *
 * @author Lion Li
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfig {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;
    private static final Color BACKGROUND = Color.LIGHT_GRAY;
    private static final Font FONT = new Font("Arial", Font.BOLD, 48);

    @Lazy
    @Bean
    public AbstractCaptcha captchaGenerator(CaptchaProperties captchaProperties) {
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();
        CodeGenerator codeGenerator = ReflectUtil.newInstance(captchaType.getClazz(), length);
        Class<?> type = captchaProperties.getCategory().getClazz();
        return getAbstractCaptcha(type, codeGenerator);
    }

    private static AbstractCaptcha getAbstractCaptcha(Class<?> type, CodeGenerator codeGenerator) {
        AbstractCaptcha captcha;
        if (type.equals(LineCaptcha.class)) {
            //线段干扰的验证码
            captcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT);
        } else if (type.equals(CircleCaptcha.class)) {
            //圆圈干扰验证码
            captcha = CaptchaUtil.createCircleCaptcha(WIDTH, HEIGHT);
        } else {
            //扭曲干扰验证码
            captcha = CaptchaUtil.createShearCaptcha(WIDTH, HEIGHT);
        }
        captcha.setGenerator(codeGenerator);
        captcha.setBackground(BACKGROUND);
        captcha.setFont(FONT);
        return captcha;
    }
}
