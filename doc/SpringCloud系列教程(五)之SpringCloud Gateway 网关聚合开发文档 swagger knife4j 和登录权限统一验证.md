# SpringCloud系列教程(五)之SpringCloud Gateway 网关聚合开发文档 swagger knife4j 和登录权限统一验证 | 8月更文挑战

> 阅读提醒：
1. 本文面向的是有一定springboot基础者
2. 本次教程使用的Spring Cloud Hoxton RELEASE版本
3. 由于knife4j比swagger更加友好，所以本文集成knife4j
4. 本文依赖上一篇的工程，请查看上一篇文章以做到无缝衔接，或者直接下载源码：[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

## 本文概览

- Spring Cloud Gateway集成Knife4j
- Spring Cloud Gateway集成登录权限统一校验

## 开始

上篇文章介绍了Spring Cloud Gateway的使用，本文将介绍如何在网关层聚合swagger文档，聚合之后可以非常方便的对开发文档进行管理，也是业界比较常用的方式。

首先在**父pom**文件`dependencyManagement` 节点中增加knife4j的依赖：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

## 配置 spring-cloud-nacos-provider和spring-cloud-nacos-consumer

> 注意，这里标题中两个工程为前几篇文章中构建的，如果不想看前几篇文章，就创建两个模块然后分别集成nacos，knife4j即可。

在两个模块中分别增加maven依赖：

```xml
<dependency>
      <groupId>com.github.xiaoymin</groupId>
      <artifactId>knife4j-spring-boot-starter</artifactId>
  </dependency>
```

在两个模块中分别新增配置类：`Knife4jConfiguration` 

```java
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
```

注意有些相关的信息需要修改。

新增配置：

```yaml
swagger:
    enable: true
```

## 配置 spring-cloud-gateway

接下来是重头戏，如何在Spring Cloud Gateway中聚合swagger文档。

首先在pom中增加maven依赖：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
</dependency>
```

新增Swagger的配置类：`SwaggerResourceConfig` 

```java
@Component
@Primary
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;
    private final GatewayProperties gatewayProperties;

    public SwaggerResourceConfig(RouteLocator routeLocator, GatewayProperties gatewayProperties) {
        this.routeLocator = routeLocator;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        //获取所有路由的ID
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        //过滤出配置文件中定义的路由->过滤出Path Route Predicate->根据路径拼接成api-docs路径->生成SwaggerResource
        gatewayProperties.getRoutes().stream().filter(routeDefinition -> routes.contains(routeDefinition.getId())).forEach(route -> {
            route.getPredicates().stream()
                    .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName()))
                    .forEach(predicateDefinition -> resources.add(swaggerResource(route.getId(),
                            predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0")
                                    .replace("**", "v2/api-docs"))));
        });

        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
```

主要的配置的作用已经在代码中进行注释。

新增一个控制器：`SwaggerHandler` 

```java
@RestController
public class SwaggerHandler {

    @Autowired(required = false)
    private SecurityConfiguration securityConfiguration;

    @Autowired(required = false)
    private UiConfiguration uiConfiguration;

    private final SwaggerResourcesProvider swaggerResources;

    @Autowired
    public SwaggerHandler(SwaggerResourcesProvider swaggerResources) {
        this.swaggerResources = swaggerResources;
    }

    /**
     * Swagger安全配置，支持oauth和apiKey设置
     */
    @GetMapping("/swagger-resources/configuration/security")
    public Mono<ResponseEntity<SecurityConfiguration>> securityConfiguration() {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(securityConfiguration).orElse(SecurityConfigurationBuilder.builder().build()), HttpStatus.OK));
    }

    /**
     * Swagger UI配置
     */
    @GetMapping("/swagger-resources/configuration/ui")
    public Mono<ResponseEntity<UiConfiguration>> uiConfiguration() {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(uiConfiguration).orElse(UiConfigurationBuilder.builder().build()), HttpStatus.OK));
    }

    /**
     * Swagger资源配置，微服务中这各个服务的api-docs信息
     */
    @GetMapping("/swagger-resources")
    public Mono<ResponseEntity> swaggerResources() {
        return Mono.just((new ResponseEntity<>(swaggerResources.get(), HttpStatus.OK)));
    }
}-----------------
```

## 测试

分别运行：spring-cloud-nacos-provider，spring-cloud-nacos-consumer，spring-cloud-gateway 三个服务。

打开swagger的地址: [http://127.0.0.1:15010/doc.html](http://127.0.0.1:15010/doc.html)

验证结果：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124615.png)

看到上图中的结果说明聚合成功。

## 集成登录权限的统一校验

> 为了直达主题，本次集成登录权限会尽量的简单，其中忽略一些细节，比如登录服务模块，可以查看demo的源码：[https://github.com/WinterChenS/spring-cloud-hoxton-study/tree/main/spring-cloud-auth](https://github.com/WinterChenS/spring-cloud-hoxton-study/tree/main/spring-cloud-auth)
并且我会在关键的点上明确讲解。

在开始之前需要明白一个原理，如果要实现统一鉴权，那么需要对所有的请求进行统一的拦截，要实现统一的拦截，在Spring Cloud Gateway中有一个filter接口为：`GlobalFilter` 。

所以我们可以通过实现`GlobalFilter` 接口来实现请求的拦截。

### 实现

新建一个类实现`GlobalFilter` 接口，并且实现`filter` 方法，在此基础上我们还需要实现`Ordered` 接口，控制拦截的优先级，鉴权拦截优先级是最高的，

```java
@Slf4j
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Autowired
    UserRedisCollection userRedisCollection;

		// (1)
    private boolean checkWhiteList(String uri) {
        boolean access = false;
        if (uri.contains("/login") || uri.contains("/v2/api-docs")) {
            access = true;
            if (uri.contains("logout")) {
                access = false;
            }
        }
        if (uri.contains("/open-api")) {
            access = true;
        }
        return access;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			  // (2)
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().getPath();
				
				// (3)
        //前端访问不到header问题
        response.getHeaders().add("Access-Control-Allow-Headers","X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, token");
        response.getHeaders().add("Access-Control-Expose-Headers", "token");
        ServerHttpRequest mutableReq = request.mutate()
                .header(DefaultConstants.IP_ADDRESS, getIpAddress(request))
                .build();
        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
        // (4)
				//检查白名单
        if (checkWhiteList(uri)) {
            return chain.filter(mutableExchange);
        }
				
				// (5)
        //从request获取token
        String accessToken = request.getHeaders().getFirst(DefaultConstants.TOKEN);
        log.info("AccessToken: [{}]", accessToken);

				// (6)
        if (StringUtils.isBlank(accessToken)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
        }
        Claims claims = null;
        try {
						// (7)
            claims = JwtUtil.parseJWT(accessToken, DefaultConstants.SECRET_KEY);
						
            if (claims == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
            }
            log.info("claims is:{}", claims);
            if (claims.getSubject().equals(DefaultConstants.USER)){
                if(claims.get(DefaultConstants.USERID)!=null) {
                    Long userId = Long.parseLong(claims.get(DefaultConstants.USERID).toString());

                    log.info("userId:{}", userId);
                    Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
                    map.put(DefaultConstants.USERID,userId.toString());
										
										// (8)
                    UserInfoEntity userInfo = userRedisCollection.getAuthUserInfoAndCache(userId);
                    //判断是否
                    if (userInfo == null) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
                    }

										// (9)
                    String token = JwtUtil.createToken(DefaultConstants.USER, map, DefaultConstants.SECRET_KEY);
                    response.getHeaders().add(DefaultConstants.TOKEN, token);
										
                    mutableReq = request.mutate().header(DefaultConstants.USER_ID, String.valueOf(userId))
                            .header(DefaultConstants.IP_ADDRESS, getIpAddress(request))
                            .build();
                    mutableExchange = exchange.mutate().request(mutableReq).build();
                    return chain.filter(mutableExchange);

                }

            }
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
        }

        return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
    }

    private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, ResultCodeEnum resultCode, String responseText) {
        serverHttpResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        CommonResult<?> result = CommonResult.failed(resultCode.getCode(), responseText);
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

    public String getIpAddress(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getHostString();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
```

- (1) 这个方法可以将白名单加入，当请求到这些url的时候不进行权限的校验
- (2) 我们在使用Spring Cloud Gateway的时候，注意到过滤器（包括GatewayFilter、GlobalFilter和过滤器链GatewayFilterChain），都依赖到ServerWebExchange，这里filter的设计跟Servlet中的的filter相似，也就是当前过滤器决定是否执行下一个过滤器的逻辑，而ServerWebExchange就是当前请求和响应的上下文，不仅包含了request和response，还包含了一些扩展方法，如代码中可以获取到request和response。
- (3) 这一步，主要是解决前端无法中header中获取到需要获取的参数。比如`token` 。
- (4) 这里对应了第一步白名单的判断，如果当前请求在白名单就跳过后面的一些权限的判断，直接执行下一个过滤器。
- (5) 这里很简单，就是从request中获取到token，方便jwt转换成对应的用户信息。
- (6) 如果请求没有携带token就不予通过。
- (7) jwt将token转换成用户信息，用于后面的判断以及用户的基本信息。
- (8) 上面获取到用户的信息之后，根据用户的userId查询redis中是否存在该用户数据，如果不存在表示该用户的用户信息已经过期了，而且从redis查询的时候会重置超时时间（也就保证了只要经常的在线就不需要重新登录，超过设定的时间没有在线，那么就需要重新登录）。注意：这里是需要跟登录服务进行联动，也就是登录成功之后将用户的信息存入redis，然后gateway这边能取到该用户信息。所以需要保证这一点。
- (9) 这里会重新颁发token，主要是防止token会过期，token的过期时间在jwtUtils中可以设置，所以，前端需要每次请求之后都将新的token作为下一次请求的token。
- 注意：本文的token是放在header中，前端小伙伴需要从header中取token。

代码中的方法：`UserRedisCollection.getAuthUserInfoAndCache(Long userId)`

```java
@Autowired
    private RedisTemplate redisTemplate;

    public UserInfoEntity getAuthUserInfoAndCache(Long userId) {
        CommonAssert.meetCondition(userId == null, "未获取到userId");
        String key = DefaultConstants.USER_INFO_REDIS + userId;
        UserInfoEntity entity = (UserInfoEntity) redisTemplate.opsForValue().get(key);
        if (null != entity) {
            redisTemplate.opsForValue().set(key, entity, 60 * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
            return entity;
        }
        CommonAssert.meetCondition(true, "当前用户未登陆，未获取到登陆信息");
        return null;
    }
```

该方法简单，就是根据userId获取到用户信息的，成功获取之后会重置超时时间，时长可以根据需要进行修改。

关于本文的登录服务，可以从demo源码中获取：

[spring-cloud-hoxton-study/spring-cloud-auth at main · WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study/tree/main/spring-cloud-auth)

### 测试

分别启动gateway,provider,auth服务。

首先，测试未登录的情况：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124637.png)

然后进入auth服务，打开登录接口：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124653.png)

打开Headers，复制token

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124712.png)

再次切换到provider服务，设置文档的全局参数：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124726.png)

然后刷新一下页面再请求就可以发现，请求成功：

![](https://cdn.jsdelivr.net/gh/WinterChenS/imgrpo/blog/20210731124735.png)

到这里就成功集成了gateway鉴权了。

### 拓展

- 至于登出的逻辑，思路是这样的，登出只需要在auth服务中将用户信息从redis清除即可，这样在gateway中查询redis就可以查到用户登录信息已经失效了。
- 如何在服务中获取当前登录用户信息呢？这个其实挺简单的，一般的做法就是写一个工具类，该工具类从请求的header中获取token，或者在请求阶段就讲userId设置到token中，如果只有token就讲token转换成用户信息，然后根据id从redis中获取用户详细信息，不过一般只需要userId就可以了，这边给个示例：

```java
public static String getUserIdByCurrent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(ConstantsUtil.USER_ID);
        }else{
            return "";
        }

    }
```

## 总结

本章介绍了如何在gateway中聚合swagger文档以及统一鉴权的集成，内容其实并不多，原本打算分开两篇进行介绍的，swagger部分没有什么需要注意的原理，索性就放在一起，真正重要的点就是Spring Cloud Gateway中的filter的应用，这部分可以通过查找资料详细的了解一下，下一篇将介绍如何使用限流组件Sentinel，这个组件由alibaba提供。

## 源码地址

[https://github.com/WinterChenS/spring-cloud-hoxton-study](https://github.com/WinterChenS/spring-cloud-hoxton-study)

参考文献

[Spring Cloud Gateway-ServerWebExchange核心方法与请求或者响应内容的修改](https://juejin.cn/post/6844903846469189645)