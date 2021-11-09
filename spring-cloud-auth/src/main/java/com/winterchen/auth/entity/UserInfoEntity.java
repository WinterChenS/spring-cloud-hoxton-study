package com.winterchen.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/29 13:49
 **/
@Data
@TableName("user_info")
@Builder
public class UserInfoEntity implements Serializable {

    private static final long serialVersionUID = 92049696933194360L;

    @TableId(value="id" ,type = IdType.AUTO)
    /** 自增主键 */
    @TableField("id")
    private Long id;

    /** 账号信息 */
    @TableField("account")
    private String account;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 密码 */
    @TableField("password")
    private String password;
}