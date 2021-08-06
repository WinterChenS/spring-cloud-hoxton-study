# SpringCloud系列教程(七)之使用Spring Cloud Sleuth+Zipkin实现链路追踪

> 阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本
3. 本文依赖上一篇的工程，请查看上一篇文章以做到无缝衔接，或者直接下载源码：[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 前情概要

- [SpringCloud系列教程(一)开篇](https://juejin.cn/post/6987998097209032741)
- [SpringCloud系列教程(二)之Nacos | 8月更文挑战](https://juejin.cn/post/6991323018802757662)
- [SpringCloud系列教程(三)之Open Feign | 8月更文挑战](https://juejin.cn/post/6991635985847025671)
- [SpringCloud系列教程(四)之SpringCloud Gateway | 8月更文挑战](https://juejin.cn/post/6992010061576929310)
- [SpringCloud系列教程(五)之SpringCloud Gateway 网关聚合开发文档 swagger knife4j 和登录权限统一验证](https://juejin.cn/post/6992404611617259534)
- [SpringCloud系列教程(六)之SpringCloud 使用sentinel作为熔断器](https://juejin.cn/post/6992404611617259534)


## 全文概要

- Sleuth快速开始
- 跟踪原理
- 与Zipkin整合

## 简介

通过前面的学习，实际上已经搭建出一个基础的微服务架构系统来实现业务要求了。但是随着业务的的发展壮大，系统的规模也越来越大，各个业务模块之间的服务调用也越来越错综复杂，通常一个请求在后端会经过多个不同的业务模块来协同产生最后的结果，在复杂的微服务架构系统中，几乎每个请求都会形成一条复杂的分布式服务调用链路，在每条链路中任何一个业务服务出现延迟或者异常都会导致整个请求的失败。所以，对于每个请求，全链路调用的追踪越来越重要了，通过实现对于请求调用的链路追踪可以帮助我们快速的定位异常的根源以及链路中的性能瓶颈。针对分布式链路追踪，目前有许多业内常用的解决方案，比如Sleuth+Zipkin，skywalking等。本文主要是针对Sleuth+Zipkin的方案。

## Sleuth快速开始

> 在整合Sleuth开始之前，友情提醒一下，为了顺畅的整合，建议查看前面的文章，因为依赖前面文章的工程。

### 工程改造

在`consumer`，`provider`，`gateway`，`auth`四个工程中增加依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

然后依次启动四个服务，如果Hystrix和Sentinel冲突可以将`feign.hystrix.enable: true` 去除，因为接入Sentinel之后就不需要Hystrix组件来实现服务熔断，反而造成组件冲突。

试着请求接口：[http://127.0.0.1:15010/consumer/nacos/echo/hello](http://127.0.0.1:15010/consumer/nacos/echo/hello)

可以看到控制台日志输出变了：

gateway:

```prolog
2021-08-04 16:57:25.166  INFO [winter-gateway,88a4de6d2424cedf,88a4de6d2424cedf,false] 23972 --- [ctor-http-nio-2] c.w.gateway.filter.AuthorizeFilter       : AccessToken: [eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhdXRoX3VzZXIiLCJleHAiOjE2MzAxMzUzNTQsIm5iZiI6MTYyNzU0MzM1NCwidXNlcklkIjoxMDAwMDAwMDF9.c09H4l_QW3v_ReNyec4nv-vqXtMDBlp4RRhh80RquPS1Ol_slH2k_dZ4vo_MYjCzJXKwWhZpt58UzgG6ZUfK8Q]
2021-08-04 16:57:25.456  INFO [winter-gateway,88a4de6d2424cedf,88a4de6d2424cedf,false] 23972 --- [ctor-http-nio-2] c.w.gateway.filter.AuthorizeFilter       : claims is:{sub=auth_user, exp=1630135354, nbf=1627543354, userId=100000001}
2021-08-04 16:57:25.457  INFO [winter-gateway,88a4de6d2424cedf,88a4de6d2424cedf,false] 23972 --- [ctor-http-nio-2] c.w.gateway.filter.AuthorizeFilter       : userId:100000001
```

consumer:

```prolog
2021-08-04 16:57:26.566  INFO [winter-nacos-consumer,88a4de6d2424cedf,1170003686062d81,false] 26520 --- [io-16011-exec-1] com.alibaba.nacos.client.naming          : new ips(1) service: DEFAULT_GROUP@@winter-nacos-provider -> [{"clusterName":"DEFAULT","enabled":true,"ephemeral":true,"healthy":true,"instanceHeartBeatInterval":5000,"instanceHeartBeatTimeOut":15000,"instanceId":"10.1.18.76#16012#DEFAULT#DEFAULT_GROUP@@winter-nacos-provider","ip":"10.1.18.76","ipDeleteTimeout":30000,"metadata":{"preserved.register.source":"SPRING_CLOUD"},"port":16012,"serviceName":"DEFAULT_GROUP@@winter-nacos-provider","weight":1.0}]
```

从上面的控制台输出内容 中 ， 我 们可 以看到多了一些形如［[winter-gateway,88a4de6d2424cedf,88a4de6d2424cedf,false]的日志信息， 而这些元素正是实现分布式服务跟踪的重要组成部分，每个值的含义如下所述。

- 第一 个值： `winter-gateway`, 它记录了应用的名称，也就是 `application.properties`中 `spring.application.name`参数配置的属性。
- 第二个值： `88a4de6d2424cedf`, `Spring Cloud Sleuth`生成的 一 个`ID`,称为`Trace ID`,它用来标识 一 条请求链路。一 条请求链路中包含 一 个`TraceID`, 多个`SpanID`。
- 第三个值： `88a4de6d2424cedf`, `Spring Cloud Sleuth`生成的另外 一 个`ID`, 称为`Span ID`, 它表示 一 个基本的工作单元， 比如发送 一 个HTTP请求。
- 第四个值： `false`, 表示是否要将该信息输出到`Zipkin`等服务中来收集和展示 。

上面四个值中的`TraceID`和`SpanID`是`Spring Cloud Sleuth`实现分布式服务跟踪的核心。 在 一 次服务请求链路的调用过程中， 会保待并传递同 一 个`Trace ID`, 从而将整个分布于不同微服务进程中的请求跟踪信息串联起来。 以上面输出内容为例， `winter-gateway` 和 `winter-nacos-consumer`同属于 一 个前端服务请求来源，所以它们的TraceID是相同的，处于同 一 条请求链路中。

### 跟踪原理:

分布式系统中的服务跟踪在理论上并不复杂， 它主要包括下面两个关键点。

- 为了实现请求跟踪， 当请求发送到分布式系统的入口端点时， 只需要服务跟踪框架为该请求创建 一 个唯 一 的跟踪标识， 同时在分布式系统内部流转的时候，框架始终保待传递 该唯 一 标识， 直到返回给请求方为止， 这个唯 一 标识就是前文中提到的Trace ID。 通过TraceID的记录， 我们就能将所有请求过程的日志关联起来。
- 为了统计各处理单元的时间延迟， 当请求到达各个服务组件时， 或是处理逻辑到达某个状态时，也通过 一 个唯 一 标识来标记它的开始、 具体过程以及结束， 该标识就是前文中提到的SpanID。 对于每个Span来说， 它必须有开始和结束 两个节点， 通过记录开始 Span和结束Span的时间戳，就能统计出该Span的时间延迟，除了时间戳记录之外，它还可以包含 一 些其他元数据， 比如事件名称、 请求信息等。

## 与Zipkin整合

## Zipkin安装

普通安装：

```prolog
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

docker-compose启动：

docker-compose.yml:

```prolog
version: '2'

services:
  zipkin:
    image: openzipkin/zipkin
    container_name: zipkin
    environment:
      - STORAGE_TYPE=mysql
      - MYSQL_DB=zipkin
      - MYSQL_USER=root
      - MYSQL_PASS=root
      - MYSQL_HOST=172.26.208.1
      - MYSQL_TCP_PORT=3306
    ports:
      - 9411:9411
```

同时支持多种数据存储方式，比如es\mysql等，更多收集数据和存储方式见：[https://github.com/openzipkin/zipkin/tree/master/zipkin-server](https://github.com/openzipkin/zipkin/tree/master/zipkin-server)

**如果使用mysql**需要新建库和表：

```sql
CREATE TABLE IF NOT EXISTS zipkin_spans (
  `trace_id_high` BIGINT NOT NULL DEFAULT 0 COMMENT 'If non zero, this means the trace uses 128 bit traceIds instead of 64 bit',
  `trace_id` BIGINT NOT NULL,
  `id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `remote_service_name` VARCHAR(255),
  `parent_id` BIGINT,
  `debug` BIT(1),
  `start_ts` BIGINT COMMENT 'Span.timestamp(): epoch micros used for endTs query and to implement TTL',
  `duration` BIGINT COMMENT 'Span.duration(): micros used for minDuration and maxDuration query',
  PRIMARY KEY (`trace_id_high`, `trace_id`, `id`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED CHARACTER SET=utf8 COLLATE utf8_general_ci;

ALTER TABLE zipkin_spans ADD INDEX(`trace_id_high`, `trace_id`) COMMENT 'for getTracesByIds';
ALTER TABLE zipkin_spans ADD INDEX(`name`) COMMENT 'for getTraces and getSpanNames';
ALTER TABLE zipkin_spans ADD INDEX(`remote_service_name`) COMMENT 'for getTraces and getRemoteServiceNames';
ALTER TABLE zipkin_spans ADD INDEX(`start_ts`) COMMENT 'for getTraces ordering and range';

CREATE TABLE IF NOT EXISTS zipkin_annotations (
  `trace_id_high` BIGINT NOT NULL DEFAULT 0 COMMENT 'If non zero, this means the trace uses 128 bit traceIds instead of 64 bit',
  `trace_id` BIGINT NOT NULL COMMENT 'coincides with zipkin_spans.trace_id',
  `span_id` BIGINT NOT NULL COMMENT 'coincides with zipkin_spans.id',
  `a_key` VARCHAR(255) NOT NULL COMMENT 'BinaryAnnotation.key or Annotation.value if type == -1',
  `a_value` BLOB COMMENT 'BinaryAnnotation.value(), which must be smaller than 64KB',
  `a_type` INT NOT NULL COMMENT 'BinaryAnnotation.type() or -1 if Annotation',
  `a_timestamp` BIGINT COMMENT 'Used to implement TTL; Annotation.timestamp or zipkin_spans.timestamp',
  `endpoint_ipv4` INT COMMENT 'Null when Binary/Annotation.endpoint is null',
  `endpoint_ipv6` BINARY(16) COMMENT 'Null when Binary/Annotation.endpoint is null, or no IPv6 address',
  `endpoint_port` SMALLINT COMMENT 'Null when Binary/Annotation.endpoint is null',
  `endpoint_service_name` VARCHAR(255) COMMENT 'Null when Binary/Annotation.endpoint is null'
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED CHARACTER SET=utf8 COLLATE utf8_general_ci;

ALTER TABLE zipkin_annotations ADD UNIQUE KEY(`trace_id_high`, `trace_id`, `span_id`, `a_key`, `a_timestamp`) COMMENT 'Ignore insert on duplicate';
ALTER TABLE zipkin_annotations ADD INDEX(`trace_id_high`, `trace_id`, `span_id`) COMMENT 'for joining with zipkin_spans';
ALTER TABLE zipkin_annotations ADD INDEX(`trace_id_high`, `trace_id`) COMMENT 'for getTraces/ByIds';
ALTER TABLE zipkin_annotations ADD INDEX(`endpoint_service_name`) COMMENT 'for getTraces and getServiceNames';
ALTER TABLE zipkin_annotations ADD INDEX(`a_type`) COMMENT 'for getTraces and autocomplete values';
ALTER TABLE zipkin_annotations ADD INDEX(`a_key`) COMMENT 'for getTraces and autocomplete values';
ALTER TABLE zipkin_annotations ADD INDEX(`trace_id`, `span_id`, `a_key`) COMMENT 'for dependencies job';

CREATE TABLE IF NOT EXISTS zipkin_dependencies (
  `day` DATE NOT NULL,
  `parent` VARCHAR(255) NOT NULL,
  `child` VARCHAR(255) NOT NULL,
  `call_count` BIGINT,
  `error_count` BIGINT,
  PRIMARY KEY (`day`, `parent`, `child`)
) ENGINE=InnoDB ROW_FORMAT=COMPRESSED CHARACTER SET=utf8 COLLATE utf8_general_ci;
```

访问控制台：[http://127.0.0.1:9411](http://127.0.0.1:9411/)

### 修改工程

在`consumer`，`provider`，`gateway`，`auth`四个工程中增加依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

四个工程分别修改配置：

```yaml
spring:
  zipkin:
    sender:
      type: web
    base-url: http://localhost:9411/
    service:
      name: consumer # 这里根据spring.application.name相同即可
```

### 测试

分别启动四个工程`consumer`，`provider`，`gateway`，`auth` 然后浏览器访问：[http://127.0.0.1:15010/consumer/nacos/feign-test/hello](http://127.0.0.1:15010/consumer/nacos/feign-test/hello)

访问：[http://127.0.0.1:9411/](http://127.0.0.1:9411/)  点击Run Query 即可查询到链路追踪数据：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210806092410.png)

 随便点击一个SHOW查看链路详细信息：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210806092422.png)

可以看到整个请求链路的过程关系，以及请求的耗时等信息。

## 总结

在复杂的微服务架构中，服务之间的链路追踪也是至关重要的，可以帮助定位服务的异常和性能瓶颈，除了Sleuth+Zipkin还可以使用其他的一些解决方案。

## 源码地址

[GitHub - WinterChenS/spring-cloud-hoxton-study: spring cloud hoxton release study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

参考文献

[https://forezp.blog.csdn.net/article/details/115632914](https://forezp.blog.csdn.net/article/details/115632914)

[https://www.cnblogs.com/wuzhenzhao/p/12762976.html](https://www.cnblogs.com/wuzhenzhao/p/12762976.html)