# SpringCloud系列教程(二)之Nacos | 8月更文挑战

> 阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本

## 本文概览

- 什么是注册中心？
- 什么是配置中心？
- 如何在springcloud中使用Nacos？

## 前言

在使用nacos之前我们需要理解nacos在整个微服务架构中担任了什么样的角色，在微服务架构中，注册中心是非常核心的基础服务之一，在微服务流行之前就已经出现在分布式架构中。比如Dubbo，Dubbo在国内是比较流行的分布式架构，也是一个非常实用的框架，提供了比较完备的服务治理功能，而服务治理的实现主要依靠注册中心。

本文中用到的demo源码地址：[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 什么是注册中心？

注册中心可以说是微服务架构中的”通讯录“，它记录了服务和服务地址的映射关系。在分布式架构中，服务会注册到这里，当服务需要调用其它服务时，就这里找到服务的地址，进行调用。

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731102123.png)

列举一个比较简单的生活例子：

1. 小明要给小红打电话，需要从通讯录(注册中心)中获取到小红的电话号码(服务地址);
2. 小明根据通讯录中的电话号码拨打小红的电话号码(服务请求);
3. 小红接通了电话，并随后完成了通话；
4. 后面再拨打小红的电话号码可以通过电话保存的号码进行拨打(本地缓存);

nacos在整个微服务架构中充当了通讯录的角色，服务之间的调用，监控等都需要通过注册中心进行获取服务的地址端口的映射，并且一般服务端都会有本地缓存，下次请求之前会查询本地的服务映射信息，可以减少网络请求提高服务的吞吐量。

## 什么是配置中心？

配置中心其实理解起来很简单，就是字面意思，把服务的配置集中在配置中心，可以实现动态的修改，避免了每次修改配置文件都需要重新发布服务的尴尬。

## 下载nacos，并启动

Nacos 致力于帮助您发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，帮助您快速实现动态服务发现、服务配置、服务元数据及流量管理。下载地址https://github.com/alibaba/nacos/releases，下载最新版的2.0版本。

下载完成后，解压，在解压后的文件的/bin目录下，windows系统点击startup.cmd就可以启动nacos。linux或mac执行以下命令启动nacos。

sh startup.sh -m standalone

登陆页面：http://localhost:8848/nacos/ 登陆用户nacos，登陆密码为nacos。

## jdk版本

- jdk： 1.8
- maven： 3.3.9
- nacos: 2.0

## springcloud 和 springboot 版本

- springcloud: Hoxton.RELEASE
- springboot: 2.2.13.RELEASE
- springcloud-alibaba: 2.2.6.RELEASE

## 配置中心

首先我们新建一个springboot 基础项目，这里项目命名为：`spring-cloud-hoxton-study`

### 项目结构：

```xml
-spring-cloud-hoxton-study
  |—spring-cloud-nacos-consumer
  |—pom.xml
```

### 父pom文件加入依赖(spring-cloud-hoxton-study)：

```xml

<properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Hoxton.RELEASE</spring-cloud.version>
        <spring-boot.version>2.2.13.RELEASE</spring-boot.version>
        <spring-cloud-alibaba.version>2.2.6.RELEASE</spring-cloud-alibaba.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <!-- springCloud依赖 -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- springBoot依赖 -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

						<!-- spring cloud alibaba 依赖 -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

### 子pom修改(spring-cloud-nacos-consumer)：parent

```xml
<parent>
    <groupId>com.winterchen</groupId>
    <artifactId>spring-cloud-hoxton-study</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
```

### 子pom加入依赖(spring-cloud-nacos-consumer)：

```xml
				<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
```

> 注意：这里只是放了主要的maven依赖，如果需要查看全部的依赖，请查看[demo的源码](https://github.com/WinterChenS/spring-cloud-hoxton-study)

### 创建配置：`bootstrap.properties` 或者 `bootstrap.yml`

bootstrap.properties

```
spring.cloud.nacos.config.server-addr=127.0.0.1:8848

spring.application.name=winter-nacos-consumer

spring.profiles.active=dev

```

bootstrap.yml

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
  application:
    name: winter-nacos-consumer
  profiles:
    active: dev
```

> 文中的：127.0.0.1:8848 就是上面搭建的nacos服务

之所以需要配置 `spring.application.name`是因为nacos默认获取配置文件是按照name作为dataId的

在 Nacos Spring Cloud 中，dataId 的完整格式如下：

```
${prefix}-${spring.profiles.active}.${file-extension}
```

- prefix 默认为 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix`来配置。
- `spring.profiles.active` 即为当前环境对应的 `profile`，详情可以参考 `Spring Boot`文档。 注意：当 `spring.profiles.active` 为空时，对应的连接符 - 也将不存在，dataId 的拼接格式变成 `${prefix}.${file-extension}`
- `file-exetension` 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension` 来配置。目前只支持 `properties` 和 `yaml` 类型。

### 在nacos配置中心创建配置文件

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731102301.png)

配置详情：

Data ID: `winter-nacos-consumer-dev.yaml`

Group: `DEFAULT_GROUP`

配置内容：

```yaml
server:
  port: 16011

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848

test:
  config:
    refresh: true
```

### 自动刷新

通过 Spring Cloud 原生注解 @RefreshScope 实现配置自动更新：

```java
@Api("nacos api")
@RestController
@RequestMapping("/nacos")
@RefreshScope
public class NacosController extends BaseController {

    @Value("${test.config.refresh:true}")
    private boolean refresh;

    @GetMapping("/")
    public CommonResult<Boolean> get() {
        return CommonResult.success(refresh);
    }

}
```

### 如何测试

我们可以通过请求上面的接口: [http://127.0.0.1:16011/nacos](http://127.0.0.1:16011/nacos)

返回的结果是：true

然后修改一下配置中心的:

```java
test:
  config:
    refresh: false
```

可以发现控制台的日志有打印：

```java
2021-07-27 11:25:13.973  INFO 17240 --- [8.25.36.41_8848] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [test.config.refresh]
```

再请求一次就可以发现结果变成了：false

## 服务注册与发现

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731102327.png)

> 仍然使用上面的项目进行

### 添加依赖：

```java
<!-- Nacos 注册中心 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### 增加配置

以下是完整的配置

```yaml
server:
  port: 16011

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848

test:
  config:
    refresh: true
```

启动类增加：`@EnableDiscoveryClient` 以启用服务注册与发现

```java
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class SpringCloudNacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudNacosApplication.class, args);
    }

}
```

接下来需要测试一下服务注册与发现的功能是否正常，所以我们需要**复制**原有的服务`spring-cloud-nacos-consumer` 并改名为：`spring-cloud-nacos-provider`

当前工程结构：

```xml
-spring-cloud-hoxton-study
  |—spring-cloud-nacos-consumer
  |—spring-cloud-nacos-provider
  |—pom.xml
```

`spring-cloud-nacos-consumer` 服务下的`NacosController` 增加方法：`echo`

```java
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
```

`spring-cloud-nacos-provider` 服务下的`NacosController` 增加方法：`echo`

```java
@RestController
@RequestMapping("/nacos")
@RefreshScope
public class NacosController {

    @Value("${test.config.refresh:true}")
    private boolean refresh;

    @GetMapping("")
    public boolean get() {
        return refresh;
    }

    @GetMapping("/echo/{string}")
    public String echo(@PathVariable String string) {
        return "Hello Nacos Discovery " + string;
    }

}

```

### 开始测试：

测试之前需要给`spring-cloud-nacos-provider` 进行配置中心的配置：

1. 修改`bootstrap.yml` 的 `spring.application.name=winter-nacos-provider` ;
2. 在配置中心新增配置文件：`winter-nacos-provider-dev.yaml` 具体的配置如下:

```yaml
server:
  port: 16012

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 118.25.36.41:8848
  profiles:
    active: dev

test:
  config:
    refresh: true
```

分别启动两个服务，然后调用接口: [http://127.0.0.1:16011/nacos/echo/hello](http://127.0.0.1:16011/nacos/echo/hello)

请求的结果：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731102354.png)

## 总结

本文介绍了springcloud如何使用nacos作为配置中心和注册中心，作为微服务中比较核心的两个基础功能，需要熟练的在实战中进行使用，后面也会陆续介绍如何与其他的组件进行组合使用。

上面使用的restTemplate进行服务的调用，这样显然在实际的使用当中是非常的不便利的，那么在实际应用中使用什么方式进行远程调用呢？下一篇将介绍如何使用open feign进行远程服务调用的。

## 源码地址

[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

参考文献：

[Nacos Spring Cloud 快速开始](https://nacos.io/zh-cn/docs/quick-start-spring-cloud.html)
