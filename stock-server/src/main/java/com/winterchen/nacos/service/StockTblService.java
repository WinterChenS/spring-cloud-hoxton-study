package com.winterchen.nacos.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.winterchen.nacos.entity.Stock;

public interface StockTblService extends IService<Stock> {


    void deduct(String commodityCode, int count);

}
