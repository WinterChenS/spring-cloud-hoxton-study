package com.winterchen.nacos.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.winterchen.nacos.entity.Order;

public interface OrderTblService extends IService<Order> {

    void placeOrder(String userId, String commodityCode, Integer count);

}
