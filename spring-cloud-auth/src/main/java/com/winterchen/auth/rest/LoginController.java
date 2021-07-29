package com.winterchen.auth.rest;

import com.winterchen.auth.entity.UserInfoEntity;
import com.winterchen.auth.request.LoginRequest;
import com.winterchen.auth.service.LoginService;
import com.winterchen.auth.utils.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/7/29 13:45
 **/
@Api(tags = "登录API")
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;


    @ApiOperation("登录")
    @PostMapping("")
    public CommonResult<UserInfoEntity> login(
            @RequestBody
            LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        return CommonResult.success(loginService.login(loginRequest, response));
    }

    @ApiOperation("登出")
    @DeleteMapping("")
    public CommonResult<?> logout() {
        loginService.logout();
        return CommonResult.success();
    }

}