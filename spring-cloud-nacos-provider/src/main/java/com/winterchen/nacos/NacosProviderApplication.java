package com.winterchen.nacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class NacosProviderApplication {



    public static void main(String[] args) {
        SpringApplication.run(NacosProviderApplication.class, args);
    }

}
