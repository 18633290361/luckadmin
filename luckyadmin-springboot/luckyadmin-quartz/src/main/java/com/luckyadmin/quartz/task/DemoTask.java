package com.luckyadmin.quartz.task;

import org.springframework.stereotype.Component;

/**
 * 定时任务调度测试
 * 
 * @author luckyadmin
 */
@Component("demoTask")
public class DemoTask
{
    public void execParams(String params) {
        System.out.println("执行有参方法：" + params);
    }

    public void execNoParams() {
        System.out.println("执行无参方法");
    }
}
