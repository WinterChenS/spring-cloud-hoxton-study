package com.winterchen.auth.exception;


import com.winterchen.auth.interfaces.ResponseCodeInterface;
import lombok.Getter;

import java.util.Map;

/**
 * business exception
 * @author winterchen
 * @date 2021/7/22
 */
@Getter
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	Long BUSI_EX = 9901L;

    //业务未确认
    public static Long BUSI_EX_UN_CONFIRM = 9801L;

    // 错误码
    private Long code;

    //错误消息
    private String msg;

    //ios的事件
    private String event;

    // 参数
    private String[] params;

    //其他信息
    private Map<String, Object> otherInfo;

    String COMMA = ",";

    /**
     * 用错误信息构造
     *
     * @param msg msg
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = BUSI_EX;
        this.msg = msg;
    }

    public BusinessException(Long code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
    
    public BusinessException(ResponseCodeInterface responseCodeInterface) {
    	super(responseCodeInterface.getMessage());
    	this.code = responseCodeInterface.getCode();
        this.msg = responseCodeInterface.getMessage();
    }
    
    public static BusinessException newDpException(Long code, String msg) {
    	return new BusinessException(code , msg);
    }
    
    public static BusinessException newDpException(ResponseCodeInterface responseCodeInterface) {
    	return new BusinessException(responseCodeInterface);
    }

    public BusinessException(Long code, String msg, String event) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.event = event;
    }

    /**
     * 创建异常
     *
     * @param msg msg
     * @param ex  异常
     */
    public BusinessException(String msg, Throwable ex) {
        super(ex);
        this.msg = msg;
    }

    /**
     * 创建异常
     *
     * @param msg    msg
     * @param params 参数
     */
    public BusinessException(String msg, String[] params) {
        this.msg = msg;
        this.params = params;
    }

    /**
     * 创建异常
     *
     * @param msg    msg
     * @param otherInfo 其他信息
     */
    public BusinessException(Long code, String msg, Map<String, Object> otherInfo) {

        this.code = code;
        this.msg = msg;
        this.otherInfo = otherInfo;
    }

    /**
     * 创建异常
     *
     * @param msg    msg
     * @param ex     异常
     * @param params 参数
     */
    public BusinessException(String msg, Throwable ex, String[] params) {
        super(ex);
        this.msg = msg;
        this.params = params;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    /**
     * 取msg
     *
     * @return 获取msg
     */
    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 取参数
     *
     * @return 去参数
     */
    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(msg);
        if (params != null && params.length > 0) {
            for (Object s : params) {
                sb.append(COMMA).append(s);
            }
        }
        return sb.toString();
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
