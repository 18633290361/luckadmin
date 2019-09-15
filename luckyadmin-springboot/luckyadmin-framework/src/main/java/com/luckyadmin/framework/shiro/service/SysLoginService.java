package com.luckyadmin.framework.shiro.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.luckyadmin.framework.util.QQConnectUtil;
import com.luckyadmin.system.domain.SysConfig;
import com.luckyadmin.system.mapper.SysConfigMapper;
import com.luckyadmin.system.mapper.SysUserMapper;
import com.luckyadmin.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.luckyadmin.common.constant.Constants;
import com.luckyadmin.common.constant.ShiroConstants;
import com.luckyadmin.common.constant.UserConstants;
import com.luckyadmin.common.enums.UserStatus;
import com.luckyadmin.common.exception.user.CaptchaException;
import com.luckyadmin.common.exception.user.UserBlockedException;
import com.luckyadmin.common.exception.user.UserDeleteException;
import com.luckyadmin.common.exception.user.UserNotExistsException;
import com.luckyadmin.common.exception.user.UserPasswordNotMatchException;
import com.luckyadmin.common.utils.DateUtils;
import com.luckyadmin.common.utils.MessageUtils;
import com.luckyadmin.common.utils.ServletUtils;
import com.luckyadmin.framework.manager.AsyncManager;
import com.luckyadmin.framework.manager.factory.AsyncFactory;
import com.luckyadmin.framework.util.ShiroUtils;
import com.luckyadmin.system.domain.SysUser;
import com.luckyadmin.system.service.ISysUserService;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;

/**
 * 登录校验方法
 * 
 * @author luckyadmin
 */
@SuppressWarnings("ALL")
@Component
public class SysLoginService
{
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    SysUserMapper userMapper;

    @Autowired
    ISysConfigService sysConfigService;

    /**
     * 登录
     */
    public SysUser login(String username, String password)
    {
        // 验证码校验
        if (!StringUtils.isEmpty(ServletUtils.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA)))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // 查询用户信息
        SysUser user = userService.selectUserByLoginName(username);

        if (user == null && maybeMobilePhoneNumber(username))
        {
            user = userService.selectUserByPhoneNumber(username);
        }

        if (user == null && maybeEmail(username))
        {
            user = userService.selectUserByEmail(username);
        }

        if (user == null)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new UserNotExistsException();
        }
        
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.delete")));
            throw new UserDeleteException();
        }
        
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.blocked", user.getRemark())));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        recordLoginInfo(user);
        return user;
    }

    private boolean maybeEmail(String username)
    {
        if (!username.matches(UserConstants.EMAIL_PATTERN))
        {
            return false;
        }
        return true;
    }

    private boolean maybeMobilePhoneNumber(String username)
    {
        if (!username.matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN))
        {
            return false;
        }
        return true;
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(SysUser user)
    {
        user.setLoginIp(ShiroUtils.getIp());
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserInfo(user);
    }


    public SysUser qqLogin(String qqOpenid) throws Exception {
        SysUser user = userMapper.selectByQQOpenId(qqOpenid);
        //写日志不要耽误我们正常的业务逻辑，所以使用异步
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getLoginName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success.qq")));
        return user;
    }
}
