package com.winterchen.nacos.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winterchen.nacos.entity.Order;
import com.winterchen.nacos.api.StockFeignClient;
import com.winterchen.nacos.mapper.OrderTblMapper;
import com.winterchen.nacos.service.OrderTblService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


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
