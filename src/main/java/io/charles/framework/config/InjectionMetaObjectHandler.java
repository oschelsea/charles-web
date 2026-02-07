package io.charles.framework.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.charles.common.exception.ServiceException;
import io.charles.common.utils.SecurityUtils;
import io.charles.framework.security.LoginUser;
import io.charles.framework.web.domain.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * Created on 2026/2/7.
 *
 * @author Chelsea
 */
public class InjectionMetaObjectHandler implements MetaObjectHandler {
    /**
     * 如果用户不存在默认注入-1代表无用户
     */
    private static final Long DEFAULT_USER_ID = -1L;

    /**
     * 插入填充方法，用于在插入数据时自动填充实体对象中的创建时间、更新时间、创建人、更新人等信息
     *
     * @param metaObject 元对象，用于获取原始对象并进行填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为创建时间和更新时间，如果创建时间不为空，则使用创建时间，否则使用当前时间
                LocalDateTime current = ObjectUtil.defaultIfNull(baseEntity.getCreateTime(), LocalDateTime.now());
                baseEntity.setCreateTime(current);

                // 如果创建人为空，则填充当前登录用户的信息
                if (ObjectUtil.isNull(baseEntity.getCreateBy())) {
                    LoginUser loginUser = getLoginUser();
                    if (ObjectUtil.isNotNull(loginUser) && StrUtil.isNotBlank(loginUser.getUsername())) {
                        // 填充创建人、更新人和创建部门信息
                        baseEntity.setCreateBy(loginUser.getUsername());
                    } else {
                        // 填充创建人、更新人和创建部门信息
                        //baseEntity.setCreateBy(DEFAULT_USER_ID);
                    }
                }
            } else {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 更新填充方法，用于在更新数据时自动填充实体对象中的更新时间和更新人信息
     *
     * @param metaObject 元对象，用于获取原始对象并进行填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为更新时间，无论原始对象中的更新时间是否为空都填充
                baseEntity.setUpdateTime(LocalDateTime.now());

                // 获取当前登录用户的ID，并填充更新人信息
                LoginUser user = getLoginUser();
                if (user != null && StrUtil.isNotBlank(user.getUsername())) {
                    baseEntity.setUpdateBy(user.getUsername());
                } else {
                    //baseEntity.setUpdateBy();
                }
            } else {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户的信息，如果用户未登录则返回 null
     */
    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            loginUser = SecurityUtils.getLoginUser();
        } catch (Exception e) {
            return null;
        }
        return loginUser;
    }
}
