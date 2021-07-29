package com.winterchen.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winterchen.auth.entity.UserInfoEntity;
import com.winterchen.auth.request.LoginRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/29 13:47
 **/
public interface LoginService extends IService<UserInfoEntity> {


    /**
     * todo 一般不会直接返回entity，而是使用response专用对象，这里是为了方便
     * @param loginRequest
     * @return
     */
    UserInfoEntity login(LoginRequest loginRequest, HttpServletResponse response);

    void logout();


}