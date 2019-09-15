package com.luckyadmin.web.controller.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.itshidu.web.tools.R;
import com.luckyadmin.framework.shiro.service.SysPasswordService;
import com.luckyadmin.framework.util.QQConnectUtil;
import com.luckyadmin.framework.web.base.BaseController;
import com.luckyadmin.system.domain.SysUser;
import com.luckyadmin.system.mapper.SysConfigMapper;
import com.luckyadmin.system.mapper.SysUserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;

@Controller
@RequestMapping("/oauth")
public class OauthController extends BaseController {

    @Autowired QQConnectUtil qqConn;

    @GetMapping("/call_qq")
    public Object qqLogin(HttpSession session) {
        String state = UUID.randomUUID().toString().replaceAll("-",""); //随机一个标识码
        String url= qqConn.getLoginURL(state);
        session.setAttribute("oauth_qq_state",state);
        System.out.println(url);
        return "redirect:"+url;
    }

    @Autowired
    SysUserMapper userMapper;
    @Autowired
    SysConfigMapper configMapper;
    @Autowired
    SysPasswordService passwordService;

    @GetMapping("/callback/qq")
    public Object qqCallback(String code, String state, HttpSession session, HttpServletRequest request) {
        System.out.println("得到AuthrizationCode:"+code);
        System.out.println("获取state："+state);

        String state2 = (String)session.getAttribute("oauth_qq_state");
        if( state2==null || !state2.equals(state) ) {//说明就没去腾讯登录
            System.out.println("QQ登录：state验证失败");
            return "redirect:/login";
        }
        session.removeAttribute("oauth_qq_state");

        try {
            //从腾讯登录成功会得到一个授权码code
            String accessToken = qqConn.getAccessToken(code);   //根据授权码获得token
            String openid = qqConn.getOpenid(accessToken);      //根据token获得openid
            QQConnectUtil.UserInfo info = qqConn.getUserInfo(openid, accessToken);  //根据openid和token获取用户信息
            System.out.println(info);

            SysUser user = userMapper.selectByQQOpenId(openid);
            if (user == null) { //如果openid查不到说明是陌生QQ
                session.setAttribute("oauth_type","qq");
                session.setAttribute("oauth_info",info);
                session.setAttribute("nickname",info.nickname); //用来在页面中显示
                session.setAttribute("qq_openid",openid);

                //session.setAttribute("oauth_info",info);

                //session.setAttribute("qq_avatar",info.figureurl_qq);
                return "/oauth_bind";
            }

            try{
                UsernamePasswordToken token = new UsernamePasswordToken("#qqOpenId", openid, false);
                Subject subject = SecurityUtils.getSubject();
                subject.login(token);
            }catch (AuthenticationException e) {
                return error("QQ互联登录失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @ResponseBody
    @PostMapping("/bind/user")
    public Object oauthBindUser(String username,String password,String way,HttpSession session) {

        SysUser user = userMapper.selectUserByLoginName(username);
        if (user == null) {
            return R.of(1, "用户名不存在");
        }
        passwordService.validate(user, password);
        if (way.equals("qq")) {
            String openid = session.getAttribute("qq_openid").toString();
            user.setQqOpenId(openid);
            userMapper.updateUser(user);
            try {
                UsernamePasswordToken token = new UsernamePasswordToken("#qqOpenId", openid, false);
                Subject subject = SecurityUtils.getSubject();
                subject.login(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (way.equals("weibo")) {
            System.out.println("微博");
        }

        return R.of(3,"绑定成功");
    }
    @ResponseBody
    @PostMapping("/bind/create")
    public Object oauthBindCreate(String username,String password,String way,HttpSession session) {
        SysUser user=new SysUser();

        String avatar=null;
        String nickname=null;

        if (way.equals("qq")) {
            QQConnectUtil.UserInfo info = (QQConnectUtil.UserInfo) session.getAttribute("oauth_info");
            String qqOpenid = session.getAttribute("qq_openid").toString();
            avatar = info.figureurl_qq;
            nickname = info.nickname;
            user.setQqOpenId(qqOpenid);

            user.setAvatar(avatar);
            user.setLoginName(username);
            user.setUserName(nickname);

            user.setSex("0");
            user.setCreateBy("system");
            user.setCreateTime(new Date());
            //默认密码加盐
            user.setSalt(UUID.randomUUID().toString());
            user.setPassword(passwordService.encryptPassword(user.getLoginName(), password, user.getSalt()));
            System.out.println("调用insertUser");
            userMapper.insertUser(user);

            try {
                UsernamePasswordToken token = new UsernamePasswordToken("#qqOpenId", qqOpenid, false);
                Subject subject = SecurityUtils.getSubject();
                subject.login(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return R.of(1,"新用户创建成功");
    }


}
