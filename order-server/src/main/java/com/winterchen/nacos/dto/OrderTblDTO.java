package com.winterchen.nacos.entity.rest.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;




@ApiModel("")
@Data
@Builder
public class OrderTblDTO implements Serializable {


    @ApiModelProperty("")
    private Integer id;

    @ApiModelProperty("")
    private String userId;

    @ApiModelProperty("")
    private String commodityCode;

    @ApiModelProperty("")
    private Integer count;

    @ApiModelProperty("")
    private Integer money;

    @Tolerate
    public OrderTblDTO(){}

}
