package com.winterchen.nacos.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author CENTURY
 * @version 1.0
 * @date 2021/11/8 10:59
 * @description TODO
 **/
@FeignClient(name = "stock-server")
public interface StockFeignClient {

    @PostMapping("/api/stock/deduct")
    Boolean deduct(@RequestParam("commodityCode") String commodityCode, @RequestParam("count") Integer count);

}