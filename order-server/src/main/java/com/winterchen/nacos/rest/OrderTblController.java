package com.winterchen.nacos.rest;

import com.winterchen.nacos.service.OrderTblService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
