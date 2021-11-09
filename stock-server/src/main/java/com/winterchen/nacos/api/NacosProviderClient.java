package com.winterchen.nacos.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/27 14:00
 * @description
 **/
@FeignClient(value = "winter-nacos-provider", fallback = NacosProviderClientFallback.class)
public interface NacosProviderClient {

    @GetMapping("/nacos/feign-test/{string}")
    String echo2(@PathVariable String string);

    @GetMapping("/nacos/ribbon-test")
    String ribbonTest();

    @GetMapping("/nacos/hystrix-test")
    String hystrixTest();

}