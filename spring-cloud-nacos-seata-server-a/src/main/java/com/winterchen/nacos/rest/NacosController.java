package com.winterchen.nacos.rest;

import com.winterchen.nacos.api.NacosProviderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private NacosProviderClient nacosProviderClient;

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

    @GetMapping("/feign-test/{str}")
    public String feignTest(@PathVariable String str) {
        return nacosProviderClient.echo2(str);
    }

    @GetMapping("/ribbon-test")
    public String ribbonTest1() {
        return nacosProviderClient.ribbonTest();
    }

    @GetMapping("/hystrix-test")
    public String hystrixTest(){
        return nacosProviderClient.hystrixTest();
    }

}