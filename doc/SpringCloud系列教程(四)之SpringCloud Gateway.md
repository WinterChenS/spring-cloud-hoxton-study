# SpringCloud系列教程(四)之SpringCloud Gateway | 8月更文挑战

> **阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本
3. 本文依赖上一篇的工程，请查看上一篇文章以做到无缝衔接，或者直接下载源码：**[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 本文概览

- Spring Cloud Gateway的简介
- gateway在微服务架构中的应用场景
- Spring Cloud Gateway使用
- 相关配置详解
- 实战中的使用

## 简介

Spring Cloud 作为新一代的微服务网关，该项目基于Spring webflux技术开发的网关，作为Spring Cloud生态系统中的网关，目标是替代zuul，为什么使用webflux？webflux是Reactor模式的响应式编程框架，底层使用了netty通信框架，非Reactor模式的zuul采用的是Tomcat容器，使用的还是传统的Servlet IO处理模型。想了解webflux和netty的同学可以搜索相关知识，这也是当前比较热门的技术。

本文中用到的demo源码地址：[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

### Gateway的特征

引用官方的文档：

> Spring Cloud Gateway features:
- Built on Spring Framework 5, Project Reactor and Spring Boot 2.0
- Able to match routes on any request attribute.
- Predicates and filters are specific to routes.
- Circuit Breaker integration.
- Spring Cloud DiscoveryClient integration
- Easy to write Predicates and Filters
- Request Rate Limiting
- Path Rewriting

- Spring Cloud Gateway 基于 Spring Framework 5, Project Reactor 和 Spring Boot 2.0
- 能够匹配任何请求属性上的路由。
- Predicates和filters特定于路由，易于编写的Predicates和filters
- Hystrix断路器的继承
- 集成了DiscoveryClient
- 具备了一些高级功：动态路由、限流、路径重写等

和zuul的功能相差不大，最主要的区别是底层通信框架的实现上。

具体的通信框架这里就暂时不展开了。

## 微服务网关的应用场景

在微服务架构中，网关起到了下层服务的路由、限流和路径重写等作用，下面用一张简单的架构图来描述一下微服务网关的作用：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731123915.png)

上图中很直观的展示了Spring Cloud Gateway在整个架构中的作用。

## Spring Cloud Gateway使用

### 新建模块

新建一个springboot工程，maven依赖如下，详细的配置可以查看demo源码

```xml

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

```

### 配置文件

修改配置文件：

```yaml
server:
  port: 15010

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 118.25.36.41:8848
    gateway:
      discovery:
        locator:
          enabled: false
          lowerCaseServiceId: true
      routes:
        - id: provider
          uri: lb://winter-nacos-provider
          predicates:
            - Path=/provider/**
          filters:
            - StripPrefix=1
        - id: consumer
          uri: lb://winter-nacos-consumer
          predicates:
            - Path=/consumer/**
          filters:
            - StripPrefix=1

  application:
    name: winter-gateway
```

具体配置的解释后面会介绍。

接下来就在启动类中加入注解: `@EnableDiscoveryClient`

```java
@EnableDiscoveryClient
@SpringBootApplication
public class SpringCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayApplication.class, args);
    }

}
```

启动服务：`spring-cloud-nacos-consumer`， `spring-cloud-nacos-provider`，`spring-cloud-gateway` 

### 测试

浏览器输入：[http://127.0.0.1:15010/consumer/nacos/echo/hello](http://127.0.0.1:15010/consumer/nacos/echo/hello)

得到返回值：`Hello Nacos Discovery hello` 

### 拓展：解决跨域问题

Spring Cloud Gateway如何解决跨域问题？我们可以在配置文件中加入：

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]': # 匹配所有请求
            allowedOrigins: "*" #跨域处理 允许所有的域
            allowedMethods: # 支持的方法
            - GET
            - POST
            - PUT
            - DELETE
```

## Spring Cloud Gateway 配置详解

我们看一下上面我们用到的配置文件：

```yaml
server:
  port: 15010

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 118.25.36.41:8848
    gateway:
      discovery:
        locator:
          enabled: false
          lowerCaseServiceId: true
      routes:
        - id: provider
          uri: lb://winter-nacos-provider
          predicates:
            - Path=/provider/**
          filters:
            - StripPrefix=1 
        - id: consumer
          uri: lb://winter-nacos-consumer
          predicates:
            - Path=/consumer/**
          filters:
            - StripPrefix=1

  application:
    name: winter-gateway
```

**id**：我们自定义的路由 ID，保持唯一

**uri**：目标服务地址

**predicates**：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。

上面这段配置的意思是，配置了一个 id 为 url-proxy-1的URI代理规则，路由的规则为：

当访问地址`http://localhost:8080/provider/nacos/echo/hello`时，

会路由到上游地址`http://winter-nacos-provider/nacos/echo/hello`。

**filters**: 过滤器是路由转发请求时所经过的过滤逻辑，可用于修改请求、响应内容，`StripPrefix=1`就代表截取路径的个数为1，比如前端过来请求`/provider/nacos/echo/hello`，匹配成功后，路由到后端的请求路径就会变成`http://127.0.0.1:16012/nacos/echo/hello`。

关于gateway的详细配置，可以参考: 

[SpringCloud gateway （史上最全）](https://www.cnblogs.com/crazymakercircle/p/11704077.html)

## 总结

关于spring cloud gateway的介绍先讲到这里，后一篇会讲解如何在网关层集成swagger文档以及如何在网关层进行登录权限的验证。并且会介绍相关的原理。

## 源码地址

[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

参考文献：

[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway#overview)

[SpringCloud gateway （史上最全）](https://www.cnblogs.com/crazymakercircle/p/11704077.html)