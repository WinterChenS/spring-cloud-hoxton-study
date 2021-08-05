# SpringCloud系列教程(六)之SpringCloud 使用sentinel作为熔断器 | 8月更文挑战

> 阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本
3. 由于knife4j比swagger更加友好，所以本文集成knife4j
4. 本文依赖上一篇的工程，请查看上一篇文章以做到无缝衔接，或者直接下载源码：[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 前情概要

-   [SpringCloud系列教程(一)开篇](https://juejin.cn/post/6987998097209032741)
-   [SpringCloud系列教程(二)之Nacos | 8月更文挑战](https://juejin.cn/post/6991323018802757662)
-   [SpringCloud系列教程(三)之Open Feign | 8月更文挑战](https://juejin.cn/post/6991635985847025671)
- [SpringCloud系列教程(四)之SpringCloud Gateway | 8月更文挑战](https://juejin.cn/post/6992010061576929310)
- [SpringCloud系列教程(五)之SpringCloud Gateway 网关聚合开发文档 swagger knife4j 和登录权限统一验证](https://juejin.cn/post/6992404611617259534)

## 本文概览

- 什么是熔断器
- 什么是sentinel
- spring cloud 整合sentinel
- 实际的应用场景

## 什么是熔断器

想必大家都知道一个生活中常见的物件，保险丝，其实它就是一种熔断器，当电器出现短路故障导致瞬间电流超过瞬间值会触发熔断，导致断开电路，从而保护了整个电路和电器的安全。

**熔断器（fuse）**是指当电流超过规定值时，以本身产生的热量使熔体熔断，断开电路的一种电器。熔断器是根据电流超过规定值一段时间后，以其自身产生的热量使熔体熔化，从而使电路断开；运用这种原理制成的一种电流保护器。熔断器广泛应用于高低压配电系统和控制系统以及用电设备中，作为短路和过电流的保护器，是应用最普遍的保护器件之一。

可能就有同学要问了，这个跟我们说的服务的熔断器有什么关系呢？其实很多原理性的事物都跟生活息息相关的，电路的熔断，保护电路和电器。在代码的世界里，可以对服务起到流量控制和降级熔断的能力，可以做到部分服务的宕机不会导致整个服务集群的雪崩。

## 什么是sentinel

sentinel是阿里巴巴开源的服务治理的框架，sentinel中文译名为哨兵，是为微服务提供流量控制、熔断降级的功能，它和Hystrix提供的功能一样，可以有效的解决微服务调用产生的“雪崩”效应，为微服务系统提供了稳定性的解决方案。

随着Hytrxi进入了维护期，不再提供新功能，Sentinel是一个不错的替代方案。通常情况，Hystrix采用线程池对服务的调用进行隔离，Sentinel才用了用户线程对接口进行隔离，二者相比，Hystrxi是服务级别的隔离，Sentinel提供了接口级别的隔离，Sentinel隔离级别更加精细，另外Sentinel直接使用用户线程进行限制，相比Hystrix的线程池隔离，减少了线程切换的开销。另外Sentinel的DashBoard提供了在线更改限流规则的配置，也更加的优化。

通过官方文档的介绍，sentinel有以下特征：

- 丰富的应用场景： Sentinel 承接了阿里巴巴近 10 年的双十一大促流量的核心场景，例如秒杀（即突发流量控制在系统容量可以承受的范围）、消息削峰填谷、实时熔断下游不可用应用等。
- 完备的实时监控： Sentinel 同时提供实时的监控功能。您可以在控制台中看到接入应用的单台机器秒级数据，甚至 500 台以下规模的集群的汇总运行情况。
- 广泛的开源生态： Sentinel 提供开箱即用的与其它开源框架/库的整合模块，例如与 Spring Cloud、Dubbo、gRPC 的整合。您只需要引入相应的依赖并进行简单的配置即可快速地接入 Sentinel。
- 完善的 SPI 扩展点： Sentinel 提供简单易用、完善的 SPI 扩展点。您可以通过实现扩展点，快速的定制逻辑。例如定制规则管理、适配数据源等。

Sentinel的主要特性：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095331.png)

### Sentinel 功能和设计理念

**流量控制**

流量控制在网络传输中是一个常用的概念，它用于调整网络包的发送数据。然而，从系统稳定性角度考虑，在处理请求的速度上，也有非常多的讲究。任意时间到来的请求往往是随机不可控的，而系统的处理能力是有限的。我们需要根据系统的处理能力对流量进行控制。Sentinel 作为一个调配器，可以根据需要把随机的请求调整成合适的形状，如下图所示：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095541.jpeg)

流量控制有以下几个角度:

- 资源的调用关系，例如资源的调用链路，资源和资源之间的关系；
- 运行指标，例如 QPS、线程池、系统负载等；
- 控制的效果，例如直接限流、冷启动、排队等。

Sentinel 的设计理念是让您自由选择控制的角度，并进行灵活组合，从而达到想要的效果。

### 熔断降级

除了流量控制以外，降低调用链路中的不稳定资源也是 Sentinel 的使命之一。由于调用关系的复杂性，如果调用链路中的某个资源出现了不稳定，最终会导致请求发生堆积。这个问题和 [Hystrix](https://github.com/Netflix/Hystrix/wiki#what-problem-does-hystrix-solve) 里面描述的问题是一样的。

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095642.png)

Sentinel 和 Hystrix 的原则是一致的: 当调用链路中某个资源出现不稳定，例如，表现为 timeout，异常比例升高的时候，则对这个资源的调用进行限制，并让请求快速失败，避免影响到其它的资源，最终产生雪崩的效果。

### Sentinel 是如何工作的

Sentinel 的主要工作机制如下：

- 对主流框架提供适配或者显示的 API，来定义需要保护的资源，并提供设施对资源进行实时统计和调用链路分析。
- 根据预设的规则，结合对资源的实时统计信息，对流量进行控制。同时，Sentinel 提供开放的接口，方便您定义及改变规则。
- Sentinel 提供实时的监控系统，方便您快速了解目前系统的状态。

## Spring Cloud 整合 Sentinel

> 注意：本文整合的工程是基于前几篇文章提供的，所以需要根据前几篇的内容一步步的搭建

### 下载安装sentinel dashboard

从官方的github仓库下载最新的release版本：[https://github.com/alibaba/Sentinel/releases](https://github.com/alibaba/Sentinel/releases)

下载完之后启动服务，端口为8748，启动命令如下：

```java
java -Dserver.port=8748 -Dcsp.sentinel.dashboard.server=localhost:8748 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.2.jar
```

启动完之后登录控制台：[http://localhost:8748](http://localhost:8748/)

账号：sentinel   密码:   sentinel

### 改造工程consumer

增加maven依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

修改配置`application.yml`(只显示部分需要修改的配置)：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    sentinel:
      transport:
        port: 18763  #1
        dashboard: 127.0.0.1:8748

feign:
  sentinel:
    enabled: true #2
```

1. 这里的 `spring.cloud.sentinel.transport.port` 端口配置会在应用对应的机器上启动一个 Http Server，该 Server 会与 Sentinel 控制台做交互。比如 Sentinel 控制台添加了一个限流规则，会把规则数据 push 给这个 Http Server 接收，Http Server 再将规则注册到 Sentinel 中。
2. 通过`feign.sentinel.enable`开启Feign和sentinel的自动适配。

### 测试

分别启动provider/consumer/gateway服务，然后**多次请求**: [http://localhost:16011/nacos/feign-test/hello](http://localhost:16011/nacos/feign-test/hello)

> 注意：需要请求接口之后才可以在控制台看到对应的服务和接口。

打开控制台可以看到：

实时监控：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095343.png)

簇点链路：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095425.png)

增加流控规则

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095437.png)

可以通过修改后面的一些控制来限制接口的一些功能，大家可以改一改尝试一下，这里就不赘述了。

> 可以给其他的服务：provider和auth也按照上面的步骤进行配置。

### Spring Cloud Gateway使用Sentinel

在工程 gateway中修改

增加maven依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

修改`application.yml`配置文件：

```yaml
spring:
  cloud:
    sentinel:
     transport:
       port: 15000
       dashboard: localhost:8748
```

创建一个网关分组和网关的限流规则：

```java
@Configuration
public class GatewayConfiguration {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        // Register the block exception handler for Spring Cloud Gateway.
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @PostConstruct
    public void doInit() {
        initCustomizedApis();
        initGatewayRules();
    }

    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("consumer")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{

                    add(new ApiPathPredicateItem().setPattern("/consumer/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        ApiDefinition api2 = new ApiDefinition("provider")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/provider/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(api1);
        definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("consumer")
                .setCount(10)
                .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("consumer")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );
        rules.add(new GatewayFlowRule("provider")
                .setCount(10)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(600)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("X-Sentinel-Flag")
                )
        );
        rules.add(new GatewayFlowRule("provider")
                .setCount(1)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pa")
                )
        );
        rules.add(new GatewayFlowRule("provider")
                .setCount(2)
                .setIntervalSec(30)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("type")
                        .setPattern("warn")
                        .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS)
                )
        );

        rules.add(new GatewayFlowRule("provider")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pn")
                )
        );
        GatewayRuleManager.loadRules(rules);
    }
}
```

### 测试

通过上面的配置gateway已经成功整合了Sentinel，然后我们可以通过请求接口：[http://127.0.0.1:15010/consumer/nacos](http://127.0.0.1:15010/consumer/nacos)  

正常的结果：

```java
{
"code": 401,
"message": "未登录"
}
```

这里返回的结果是因为之前gateway做了登录权限的校验，可以通过auth服务登录之后，将header中的token值作为请求的header中的token，就可以得到正确的返回值，对于当前的测试这个都是无关紧要的，可以通过查看前面的教程知道前因后果。

通过修改限流：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210805095453.png)

多次点击可以得到异常信息:

```java
Blocked by Sentinel: FlowException
```

这样就可以通过网关层进行统一的接口流量控制，当然Sentinel的作用不止于此，大家可以通过其他的功能掌握对于接口的控制。

## 应用场景

通过接口的限流和熔断可以让微服务中的模块可以更加的稳定，当大量流量来袭的时候也丝毫不慌。

除了一些比较极端的比如秒杀抢购抢券等功能，在流量比较大的应用中也是广泛的使用。

## 源码地址

[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

参考文献

[introduction](https://sentinelguard.io/zh-cn/docs/introduction.html)

[SpringCloud 2020版本教程3：使用sentinel作为熔断器_方志朋的专栏-CSDN博客](https://forezp.blog.csdn.net/article/details/115632888)