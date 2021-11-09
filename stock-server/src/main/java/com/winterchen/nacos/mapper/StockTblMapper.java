package com.winterchen.nacos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winterchen.nacos.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockTblMapper extends BaseMapper<Stock> {


}