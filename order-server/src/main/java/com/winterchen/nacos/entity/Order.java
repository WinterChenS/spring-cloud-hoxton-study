package com.winterchen.nacos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.math.BigDecimal;


/**
* 
*/
@Data
@TableName("order_tbl")
@Accessors(chain = true)
@Builder
public class Order implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(value="id" ,type = IdType.AUTO)
    /**  */
    @TableField("id")
    private Integer id;

    /**  */
    @TableField("user_id")
    private String userId;

    /**  */
    @TableField("commodity_code")
    private String commodityCode;

    /**  */
    @TableField("count")
    private Integer count;

    /**  */
    @TableField("money")
    private BigDecimal money;


    @Tolerate
    public Order(){}
}
