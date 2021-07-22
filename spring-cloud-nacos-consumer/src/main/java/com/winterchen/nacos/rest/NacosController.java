package com.winterchen.nacos.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate;

    @Autowired
    public NacosController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    @GetMapping("")
    public boolean get() {
        return refresh;
    }

    @GetMapping("/echo/{str}")
    public String echo(@PathVariable String str) {
        return restTemplate.getForObject("http://winter-nacos-provider/nacos/echo/" + str, String.class);
    }

}