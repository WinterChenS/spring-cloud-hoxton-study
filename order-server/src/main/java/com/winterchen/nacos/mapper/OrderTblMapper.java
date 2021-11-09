package com.winterchen.nacos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winterchen.nacos.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderTblMapper extends BaseMapper<Order> {


}