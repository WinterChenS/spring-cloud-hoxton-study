package com.winterchen.gateway.filter;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.winterchen.auth.constants.DefaultConstants;
import com.winterchen.auth.entity.UserInfoEntity;
import com.winterchen.auth.enums.ResultCodeEnum;
import com.winterchen.auth.utils.CommonResult;
import com.winterchen.auth.utils.JwtUtil;
import com.winterchen.gateway.redis.UserRedisCollection;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/4/26 1:56 下午
 * @description 登录过滤器
 **/
@Slf4j
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {


    @Autowired
    UserRedisCollection userRedisCollection;



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

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().getPath();

        //前端访问不到header问题
        response.getHeaders().add("Access-Control-Allow-Headers","X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, token");
        response.getHeaders().add("Access-Control-Expose-Headers", "token");
        ServerHttpRequest mutableReq = request.mutate()
                .header(DefaultConstants.IP_ADDRESS, getIpAddress(request))
                .build();
        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
        //检查白名单
        if (checkWhiteList(uri)) {
            return chain.filter(mutableExchange);
        }

        //从request获取token
        String accessToken = request.getHeaders().getFirst(DefaultConstants.TOKEN);
        log.info("AccessToken: [{}]", accessToken);

        if (StringUtils.isBlank(accessToken)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
        }
        Claims claims = null;
        try {
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

                    UserInfoEntity userInfo = userRedisCollection.getAuthUserInfoAndCache(userId);
                    //判断是否
                    if (userInfo == null) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return getVoidMono(response, ResultCodeEnum.UNAUTHORIZED, "未登录");
                    }

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
