package com.winterchen.auth.utils;


import com.winterchen.auth.enums.ResultCodeEnum;
import com.winterchen.auth.interfaces.ResponseCodeInterface;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 通用返回对象
 * Created by macro on 2019/4/19.
 */
@Data
public class CommonResult<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private long code;
    private String message;
    private T data;
    private Map<String , Object> otherInfo;
    
    public CommonResult() {
    }

    protected CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    protected CommonResult(long code, String message) {
        this.code = code;
        this.message = message;
    }


    protected CommonResult(long code, String message, Map<String, Object> otherInfos) {
        this.code = code;
        this.message = message;
        this.otherInfo = otherInfos;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 是否是成功的请求
     * @return
     */
    public boolean requestIsSuccess() {
        return ResultCodeEnum.SUCCESS.getCode() == code;
    }


    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage());
    }

    /**
     * 成功返回结果
     * @param  message 提示信息
     */
    public static <T> CommonResult<T> successOfMsg(String message) {
        return new CommonResult<T>(ResultCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param  message 提示信息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    /**
     * 自定义的返回结果
     */
    public static <T> CommonResult<T> returnMsg(T data, long code , String message) {
        return new CommonResult<T>(code, message, data);
    }
    
    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> failed(ResponseCodeInterface responseCodeInterface) {
        return new CommonResult<T>(responseCodeInterface.getCode(), responseCodeInterface.getMessage());
    }
    
    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> failed(ResponseCodeInterface responseCodeInterface , String message) {
        return new CommonResult<T>(responseCodeInterface.getCode() , message);
    }
    
    /**
     * 失败返回结果
     * @param code 错误码
     * @param message 返回信息
     */
    public static <T> CommonResult<T> failed(long code, String message) {
        return new CommonResult<T>(code, message);
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(ResultCodeEnum.FAILED.getCode(), message);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCodeEnum.FAILED);
    }

    /**
     * 业务异常
     */
    public static <T> CommonResult<T> businessFailed(String message) {
        return new CommonResult<T>(ResultCodeEnum.BUSINESS_FAILED.getCode(), message);
    }

    /**
     * 业务异常
     */
    public static <T> CommonResult<T> businessFailed(Long code, String message, Map<String, Object> otherInfo) {
        return new CommonResult<T>(code, message, otherInfo);
    }


    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed() {
        return failed(ResultCodeEnum.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>(ResultCodeEnum.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>(ResultCodeEnum.UNAUTHORIZED.getCode(), ResultCodeEnum.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>(ResultCodeEnum.FORBIDDEN.getCode(), ResultCodeEnum.FORBIDDEN.getMessage(), data);
    }


    
}
