package com.luckyadmin.web.controller.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.itshidu.web.tools.R;
import com.luckyadmin.framework.shiro.service.SysLoginService;
import com.luckyadmin.framework.shiro.service.SysPasswordService;
import com.luckyadmin.framework.util.QQConnectUtil;
import com.luckyadmin.system.domain.SysUser;
import com.luckyadmin.system.mapper.SysConfigMapper;
import com.luckyadmin.system.mapper.SysUserMapper;
import com.luckyadmin.system.service.ISysConfigService;
import com.luckyadmin.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.luckyadmin.common.base.AjaxResult;
import com.luckyadmin.common.utils.ServletUtils;
import com.luckyadmin.common.utils.StringUtils;
import com.luckyadmin.framework.web.base.BaseController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.UUID;

/**
 * 登录验证
 * 
 * @author luckyadmin
 */
@Controller
public class SysLoginController extends BaseController {

    @Autowired
    SysLoginService loginService;
    @Autowired
    ISysConfigService sysConfigService;

    @GetMapping("/login")
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        // 如果是Ajax请求，返回Json字符串。
        if (ServletUtils.isAjaxRequest(request)) {

            return R.of(1,"未登录或登录超时。请重新登录");
        }

        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public AjaxResult ajaxLogin(String username, String password, Boolean rememberMe)
    {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return success();
        }
        catch (AuthenticationException e) {
            String msg = "用户或密码错误";
            if (StringUtils.isNotEmpty(e.getMessage())) {
                msg = e.getMessage();
            }
            return error(msg);
        }
    }

    @GetMapping("/unauth")
    public String unauth() {
        return "/error/unauth";
    }


}
