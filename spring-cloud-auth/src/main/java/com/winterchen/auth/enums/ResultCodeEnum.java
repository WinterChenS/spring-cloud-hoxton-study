package com.winterchen.auth.enums;


import com.winterchen.auth.interfaces.ResponseCodeInterface;


public enum ResultCodeEnum implements ResponseCodeInterface {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    BUSINESS_FAILED(9901, "业务异常"),
    SYSTEM_FAILED(9999, "系统异常"),
    FORBIDDEN(403, "没有相关权限");
    private long code;
    private String message;

    private ResultCodeEnum(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
