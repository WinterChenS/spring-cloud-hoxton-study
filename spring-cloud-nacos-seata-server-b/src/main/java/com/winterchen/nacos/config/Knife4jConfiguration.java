package com.winterchen.nacos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/28 15:00
 * @description knife4j 配置
 **/
@Configuration
public class Knife4jConfiguration {

    @Value("${swagger.enable:true}")
    private boolean enableSwagger;

    @Bean(value = "defaultApi2")
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                            .title("provider服务")
                            .version("1.0")
                            .build())
                .enable(enableSwagger)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.winterchen.nacos.rest"))
                .paths(PathSelectors.any())
                .build();
    }

}