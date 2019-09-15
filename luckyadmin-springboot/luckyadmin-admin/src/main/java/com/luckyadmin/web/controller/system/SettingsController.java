package com.luckyadmin.web.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SettingsController {

    @RequestMapping("/system/settings")
    public String settings() {
        return "/system/settings/settings";
    }
}
