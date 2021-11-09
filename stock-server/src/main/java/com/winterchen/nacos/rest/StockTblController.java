package com.winterchen.nacos.rest;

import com.winterchen.nacos.service.StockTblService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
