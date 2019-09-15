package com.luckyadmin.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@MapperScan("com.luckyadmin.*.mapper")
public class MybatisPlusConfig {

}
