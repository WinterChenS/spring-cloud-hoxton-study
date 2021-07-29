package com.winterchen.auth.aop;


import com.winterchen.auth.enums.ResultCodeEnum;
import com.winterchen.auth.exception.BusinessException;
import com.winterchen.auth.utils.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/4/27 9:41 上午
 * @description 统一异常处理
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAspect {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<?> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("throw business exception", ex);
        return CommonResult.failed(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(value= {MethodArgumentNotValidException.class , BindException.class})
    public CommonResult<?> handleVaildException(Exception e){
        log.error("request params error", e);
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException)e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException)e).getBindingResult();
        }
        Map<String,String> errorMap = new HashMap<>(16);
        bindingResult.getFieldErrors().forEach((fieldError)->
                errorMap.put(fieldError.getField(),fieldError.getDefaultMessage())
        );
        return CommonResult.failed(ResultCodeEnum.VALIDATE_FAILED.getCode(), errorMap.toString());
    }


    @ExceptionHandler(Throwable.class)
    public CommonResult<?> handleBaseException(Throwable e, HttpServletRequest request) {
        log.error("throw error", e);
        return CommonResult.failed(ResultCodeEnum.SYSTEM_FAILED);
    }

}
