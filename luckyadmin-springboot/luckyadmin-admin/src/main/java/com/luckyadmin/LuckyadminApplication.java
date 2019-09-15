package com.luckyadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启动程序
 * 
 * @author luckyadmin
 */
//@EnableTransactionManagement
//@MapperScan("com.luckyadmin.*.mapper")
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class LuckyadminApplication
{
    public static void main(String[] args)
    {
        System.setProperty("spring.devtools.restart.enabled", "true");
        SpringApplication.run(LuckyadminApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  LuckyAdmin启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}