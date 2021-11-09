package com.winterchen.nacos.rest.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;




@ApiModel("")
@Data
@Builder
public class StockTblDTO implements Serializable {


    @ApiModelProperty("")
    private Integer id;

    @ApiModelProperty("")
    private String commodityCode;

    @ApiModelProperty("")
    private Integer count;

    @Tolerate
    public StockTblDTO(){}

}
