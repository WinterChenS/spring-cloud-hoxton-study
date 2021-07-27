package com.winterchen.nacos.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/22 11:07
 **/
@RestController
@RequestMapping("/nacos")
@RefreshScope
public class NacosController {

    @Value("${test.config.refresh:true}")
    private boolean refresh;

    @Value("${server.port}")
    String port;

    @GetMapping("")
    public boolean get() {
        return refresh;
    }

    @GetMapping("/echo/{string}")
    public String echo(@PathVariable String string) {
        return "Hello Nacos Discovery " + string;
    }

    @GetMapping("feign-test/{string}")
    public String feignTest(@PathVariable String string) {
        return "Hello feign " + string;
    }


    @GetMapping("/ribbon-test")
    public String ribbonTest() {
        return "Hello ribbon , my port: " + port;
    }

    @GetMapping("/hystrix-test")
    public String hystrixTest() {
        throw new RuntimeException("ex");
    }

}