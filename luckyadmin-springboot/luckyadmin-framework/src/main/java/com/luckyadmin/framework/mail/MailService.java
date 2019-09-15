package com.luckyadmin.framework.mail;

import com.luckyadmin.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service("mail")
public class MailService {

    @Autowired
    ISysConfigService configService;


    public void createSender() {
        String host = configService.selectConfigByKey("sys.mail.host");
        String username = configService.selectConfigByKey("sys.mail.username");
        String password = configService.selectConfigByKey("sys.mail.password");
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setUsername(username);
        sender.setPassword(password);

    }



}
