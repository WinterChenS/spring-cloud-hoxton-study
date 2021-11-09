package com.winterchen.nacos.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winterchen.nacos.entity.Stock;
import com.winterchen.nacos.mapper.StockTblMapper;
import com.winterchen.nacos.service.StockTblService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
