package com.winterchen.nacos.api;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/27 16:39
 * @description
 **/
@Component
public class NacosProviderClientFallback implements NacosProviderClient{
    @Override
    public String echo2(String string) {
        return "error";
    }

    @Override
    public String ribbonTest() {
        return "error";
    }

    @GetMapping("/hystrix-test")
    public String hystrixTest() {
        return "hystrix error";
    }
}