# SpringCloud系列教程(八)之整合seata分布式事务

> 两个月没有更新了，这次趁着刷技术文章的机会，把目前比较热门的分布式事务框架seata整合一下，分布式事务的出现是因为微服务导致业务分部在不同的服务中，不能像本地事务一样使用事务。
> 

阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本
3. 本文依赖上一篇的工程，请查看上一篇文章以做到无缝衔接，或者直接下载源码：

[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 什么是seata？

Seata 是一款开源的分布式事务解决方案，致力于提供高性能和简单易用的分布式事务服务。Seata 将为用户提供了 AT、TCC、SAGA 和 XA 事务模式，为用户打造一站式的分布式解决方案。

关于更多的原理可以参考官方文档，这里就不赘述了：[Seata 是什么](http://seata.io/zh-cn/docs/overview/what-is-seata.html)

## 下载安装seata-server

### 下载

下载地址：[Releases · seata/seata (github.com)](https://github.com/seata/seata/releases)

window下载zip，linux/mac下载tar.gz

> 注意：如何安装nacos请看这：[Nacos 快速入门](https://nacos.io/zh-cn/docs/quick-start.html)，建议查看前面的文章以做到丝滑入戏。
> 

### 安装

解压之后修改配置文件registry.conf（这里主要是配置nacos作为配置中心）：

```protobuf
config {
  type = "nacos" ## 这里修改为nacos，并且修改下面对应的配置

  nacos {
    serverAddr = "127.0.0.1:8848"
    namespace = "public"
    group = "SEATA_GROUP"
    username = "nacos"
    password = "nacos"
  }
}
```

然后继续修改注册中心（nacos作为注册中心）：

```protobuf
registry {
 
  type = "nacos"

  nacos {
    application = "seata-server"
    serverAddr = "127.0.0.1:8848"
    group = "DEFAULT_GROUP" #这里的group需要与业务服务在一个group内
    namespace = "public"
    cluster = "default"
    username = "nacos"
    password = "nacos"
  }
}
```

本文是以nacos作为注册中心和配置中心的，如果需要其它的方式可以查看官方文档。

### 上传配置

在上传之前先下载对应的配置文件模板

1. 首先下载config.txt文件：[seata/script/config-center at develop · seata/seata (github.com)](https://github.com/seata/seata/tree/develop/script/config-center) ，将其放入到seata的解压目录下；见图例1
2. 然后在目录下找到对应的配置中心的目录下的shell脚本，这里使用的是nacos-config.sh [seata/script/config-center/nacos at develop · seata/seata (github.com)](https://github.com/seata/seata/tree/develop/script/config-center/nacos)，将其放入到`${SEATA_DIR}/script/config-center/nacos/nacos-config.sh` 注意：`${SEATA_DIR}` 是seata的根目录；见图例2
3.  到seata的解压根目录下运行 `sh script/config-center/nacos/nacos-config.sh -h 127.0.0.1 -p 8848 -g SEATA_GROUP -u nacos -w nacos` 注意：nacos-config.sh是第2步下载的脚本文件，见图例3

图例1：

![图例1](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104117.png)

图例2：

![图例2](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104139.png)

图例3：

![图例3](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104156.png)

图例4：

![图例4](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104216.png)

在nacos控制台（localhost:8848/nacos，账密：nacos/nacos）：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104233.png)

可以看到配置已经成功上传

具体的配置请查看：[seata/config.txt at develop · seata/seata (github.com)](https://github.com/seata/seata/blob/develop/script/config-center/config.txt)

配置参数官方对照表：[Seata 参数配置](http://seata.io/zh-cn/docs/user/configurations.html)

注意修改对应数据库的配置，可以直接在nacos中修改配置。

> 有一个比较关键的点，也是很容易出错的点，有一个配置参数单独拿出来讲一下，
`service.vgroupMapping.<你的服务名称>-group=default` 这个分组需要seata-server和client都保持一致，当然，seata是可以存在多个事务分组的，比如我们订单业务涉及到库存等等逻辑，那么将这些在同一个事务的服务加入到同一个事务分组内，就如默认的配置文件中设置为：`service.vgroupMapping.my_test_tx_group=default` 那么我们需要在服务的application.yml中配置该组：
> 

```yaml
seata:
  tx-service-group: my_test_tx_group
```

## 启动seata-server：

windows：打开控制台：`./seata-server.bat -h 127.0.0.1`

linux/macos: `sh [seata-server.sh](http://seata-server.sh) -h 127.0.0.1`

成功启动：

![成功启动](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104300.png)

nacos注册中心：

![nacos注册中心](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104319.png)

### 多种部署方式：

- [使用 Docker 部署 Seata Server](http://seata.io/zh-cn/docs/ops/deploy-by-docker.html)
- [使用 Kubernetes 部署 Seata Server](http://seata.io/zh-cn/docs/ops/deploy-by-kubernetes.html)
- [使用 Helm 部署 Seata Server](http://seata.io/zh-cn/docs/ops/deploy-by-helm.html)
- [Seata 高可用部署](http://seata.io/zh-cn/docs/ops/deploy-ha.html)

### 新增seata的数据表：

对应的sql文件请查看：[seata/script/server/db at 1.4.0 · seata/seata (github.com)](https://github.com/seata/seata/tree/1.4.0/script/server/db)

下载之后在数据库中新建库：seata，然后将建库脚本导入。

## 工程改造

### 1.复制工程

将工程：spring-cloud-nacos-consumer 复制一份，改为：order-server

将工程：spring-cloud-nacos-provider 复制一份，改为：stock-server

注意：复制之后需要修改部分pom配置才可以（改为对应的名称）：

```xml
<artifactId>order-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>order-server</name>
    <description>Demo project for Spring Boot</description>
```

并且在父pom中加入：

```xml
<modules>
    ...
    <module>stock-server</module>
    <module>order-server</module>
</modules>
```

然后重新导入依赖即可

如果还存在异常，请将.imi文件删除

### 2.增加依赖

在两个工程模块的pom中增加依赖：

```xml
<!-- seata -->
        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
            <version>1.4.2</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>io.seata</groupId>
                    <artifactId>seata-spring-boot-starter</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

### 3.修改工程的application.yml配置

order-server

```yaml
logging:
  level:
    io:
      seata: debug
seata:
  tx-service-group: my_test_tx_group
```

stock-server

```yaml
logging:
  level:
    io:
      seata: debug
seata:
  tx-service-group: my_test_tx_group
```

### 4.数据库初始化

```sql
-- 创建 order库、业务表、undo_log表
create database seata_order;
use seata_order;

DROP TABLE IF EXISTS `order_tbl`;
CREATE TABLE `order_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  `money` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- 创建 stock库、业务表、undo_log表
create database seata_stock;
use seata_stock;

DROP TABLE IF EXISTS `stock_tbl`;
CREATE TABLE `stock_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- 初始化库存模拟数据
INSERT INTO seata_stock.stock_tbl (id, commodity_code, count) VALUES (1, 'product-1', 9999999);
INSERT INTO seata_stock.stock_tbl (id, commodity_code, count) VALUES (2, 'product-2', 0);
```

### 5.引入mybatis plus

父pom引入依赖：

```xml
<!-- mybatis plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.4.3.4</version>
</dependency>

<!-- 提供mysql驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.16</version>
</dependency>
```

两个工程分别引入依赖:

```xml
<!-- mybatis plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### 6.order-server业务修改

新增部分类

```java
@Data
@TableName("order_tbl")
@Accessors(chain = true)
@Builder
public class Order implements Serializable {

   private static final longserialVersionUID= 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(value="id" ,type = IdType.AUTO)
/**  */
@TableField("id")
    private Integer id;

/**  */
@TableField("user_id")
    private String userId;

/**  */
@TableField("commodity_code")
    private String commodityCode;

/**  */
@TableField("count")
    private Integer count;

/**  */
@TableField("money")
    private BigDecimal money;

    @Tolerate
    public Order(){}
}
```

```java
@Mapper
public interface OrderTblMapper extends BaseMapper<Order> {

}
```

mapperxml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.winterchen.nacos.mapper.OrderTblMapper">

</mapper>
```

service:

```java
public interface OrderTblService extends IService<Order> {

    void placeOrder(String userId, String commodityCode, Integer count);

}

```

```java
@Service
public class OrderTblServiceImpl extends ServiceImpl<OrderTblMapper, Order> implements OrderTblService {

    @Autowired
    private StockFeignClient stockFeignClient;

    /**
     * 下单：创建订单、减库存，涉及到两个服务
     *
     * @param userId
     * @param commodityCode
     * @param count
     */
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void placeOrder(String userId, String commodityCode, Integer count) {
        BigDecimal orderMoney = new BigDecimal(count).multiply(new BigDecimal(5));
        Order order = new Order().setUserId(userId).setCommodityCode(commodityCode).setCount(count).setMoney(orderMoney);
        baseMapper.insert(order);
        stockFeignClient.deduct(commodityCode, count);
    }

}
```

`StockFeignClient`

```java
@FeignClient(name = "stock-server")
public interface StockFeignClient {

    @PostMapping("/api/stock/deduct")
    Boolean deduct(@RequestParam("commodityCode") String commodityCode, @RequestParam("count") Integer count);

}
```

```java
@Api(tags="订单API")
@RestController
@RequestMapping("/api/order")
public class OrderTblController {

    @Autowired
    private OrderTblService orderTblService;

    /**
     * 下单：插入订单表、扣减库存，模拟回滚
     *
     * @return
     */
    @PostMapping("/placeOrder/commit")
    public Boolean placeOrderCommit() {

        orderTblService.placeOrder("1", "product-1", 1);
        return true;

    }

    /**
     * 下单：插入订单表、扣减库存，模拟回滚
     *
     * @return
     */
    @PostMapping("/placeOrder/rollback")
    public Boolean placeOrderRollback() {
        // product-2 扣库存时模拟了一个业务异常,
        orderTblService.placeOrder("1", "product-2", 1);
        return true;
    }

    @PostMapping("/placeOrder")
    public Boolean placeOrder(String userId, String commodityCode, Integer count) {
        orderTblService.placeOrder(userId, commodityCode, count);
        return true;
    }
}
```

### 7.stock-server业务修改

entity:

```java
@Data
@TableName("stock_tbl")
@Accessors(chain = true)
@Builder
public class Stock implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(value="id" ,type = IdType.AUTO)
    /**  */
    @TableField("id")
    private Integer id;

    /**  */
    @TableField("commodity_code")
    private String commodityCode;

    /**  */
    @TableField("count")
    private Integer count;

    @Tolerate
    public Stock(){}
}
```

mapper:

```java
@Mapper
public interface StockTblMapper extends BaseMapper<Stock> {

}
```

mapperxml:

```java
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.winterchen.nacos.mapper.StockTblMapper">

</mapper>
```

service:

```java
public interface StockTblService extends IService<Stock> {

    void deduct(String commodityCode, int count);

}
```

```java
@Service
public class StockTblServiceImpl extends ServiceImpl<StockTblMapper, Stock> implements StockTblService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deduct(String commodityCode, int count) {
        if (commodityCode.equals("product-2")) {
            throw new RuntimeException("异常:模拟业务异常:stock branch exception");
        }

        QueryWrapper<Stock> wrapper = new QueryWrapper<>();
        wrapper.setEntity(new Stock().setCommodityCode(commodityCode));
        Stock stock = baseMapper.selectOne(wrapper);
        stock.setCount(stock.getCount() - count);

        baseMapper.updateById(stock);
    }
}
```

controller：

```java
@Api(tags="库存API")
@RestController
@RequestMapping("/api/stock")
public class StockTblController {

    @Autowired
    private StockTblService stockTblService;

    /**
     * 减库存
     *
     * @param commodityCode 商品代码
     * @param count         数量
     * @return
     */
    @PostMapping(path = "/deduct")
    public Boolean deduct(String commodityCode, Integer count) {
        stockTblService.deduct(commodityCode, count);
        return true;
    }

}
```

### 8. 修改gateway：

修改`GatewayConfiguration`

`initCustomizedApis` 新增对`order-server`和`stock-server`的初始化

```java
ApiDefinition api3 = new ApiDefinition("order")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{

                    add(new ApiPathPredicateItem().setPattern("/order/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        ApiDefinition api4 = new ApiDefinition("stock")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/stock/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
  definitions.add(api3);
  definitions.add(api4);
```

`initGatewayRules` 新增规则

```java
rules.add(new GatewayFlowRule("order")
                .setCount(10)
                .setIntervalSec(1)
        );
        rules.add(new GatewayFlowRule("order")
                .setCount(2)
                .setIntervalSec(2)
                .setBurst(2)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
                )
        );

rules.add(new GatewayFlowRule("stock")
                .setCount(10)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(600)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("X-Sentinel-Flag")
                )
        );
        rules.add(new GatewayFlowRule("stock")
                .setCount(1)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pa")
                )
        );
        rules.add(new GatewayFlowRule("stock")
                .setCount(2)
                .setIntervalSec(30)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("type")
                        .setPattern("warn")
                        .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_CONTAINS)
                )
        );

        rules.add(new GatewayFlowRule("stock")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pn")
                )
        );
```

`appilcation.yml`新增对`order-server`和`stock-server`的路由配置:

```yaml

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
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
            - StripPrefix=1 #StripPrefix=1就代表截取路径的个数为1，比如前端过来请求/test/good/1/view，匹配成功后，路由到后端的请求路径就会变成http://localhost:8888/good/1/view
        - id: consumer
          uri: lb://winter-nacos-consumer
          predicates:
            - Path=/consumer/**
          filters:
            - StripPrefix=1
        - id: auth
          uri: lb://auth
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
        - id: order-server   -------新增order-server的路由规则
          uri: lb://order-server
          predicates:
            - Path=/order/**
          filters:
            - StripPrefix=1
        - id: stock-server  --------- 新增stock-server的路由规则
          uri: lb://stock-server
          predicates:
            - Path=/stock/**
          filters:
            - StripPrefix=1
```

## 测试：

首先启动对应的服务：

- spring-cloud-gateway
- spring-cloud-auth
- order-server
- stock-server

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104335.png)

然后打开swagger进行测试: [consumer服务](http://127.0.0.1:15010/doc.html#/order-server/%E8%AE%A2%E5%8D%95API/placeOrderUsingPOST)

### 测试提交：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104403.png)

订单创建成功：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104411.png)

库存扣减成功：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104419.png)

### 测试回滚：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20211109104428.png)

此时数据库里面都正常回滚。

## 总结

以上就是本教程的全部内容了，seata是一款非常好用的分布式事务框架，为开发人员提供了比较简单的API，seata默认使用的是AT模式的事务，当然，可以结合自身的业务选择比较合适的分布式事务模式，具体的配置可以参考官方文档。

本项目的源码地址为：[WinterChenS/spring-cloud-hoxton-study: spring cloud hoxton release study (github.com)](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 参考文献：

[Seata 是什么](http://seata.io/zh-cn/docs/overview/what-is-seata.html)

[Seata（Fescar）分布式事务 整合 Spring Cloud](http://seata.io/zh-cn/blog/integrate-seata-with-spring-cloud.html)