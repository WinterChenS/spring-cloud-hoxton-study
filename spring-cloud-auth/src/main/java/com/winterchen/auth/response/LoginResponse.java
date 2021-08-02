package com.winterchen.auth.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/29 14:03
 **/
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = -217270744664811024L;

    private String token;
}