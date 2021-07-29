package com.winterchen.auth.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author CENTURY
 * @version 1.0
 * @date 2021/7/29 13:55
 **/
@Data
@ApiModel("登录信息")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 6242212367239952776L;


    @NotBlank(message = "账号不能为空")
    @ApiModelProperty("账号")
    private String account;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty("密码")
    private String password;



}