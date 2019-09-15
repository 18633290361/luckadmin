package com.luckyadmin.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.luckyadmin.system.service.ISysConfigService;

/**
 * luckyadmin首创 html调用 thymeleaf 实现参数调用
 * @author luckyadmin
 */
@Service("config")
public class ConfigService
{
    @Autowired
    private ISysConfigService configService;

    /**
     * 根据键名查询参数配置信息
     * @param configKey 参数名称
     * @return 参数键值
     */
    public String get(String configKey)
    {
        return configService.selectConfigByKey(configKey);
    }
}
