package com.winterchen.auth.utils;

import com.winterchen.auth.constants.DefaultConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;


@Component
public class UserInfoUtils {

    /**
     * 从请求头里面获取用户id，注意不是任何情况都能获取到，需要请求头中带着才可以
     * @return
     */
    public static String getUserIdByCurrent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(DefaultConstants.USER_ID);
        }else{
            return "";
        }

    }

    public static String getUserTypeByCurrent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(DefaultConstants.USER_TYPE);
        }else{
            return "";
        }
    }

    public static Long getLongUserIdByCurrent() {
        return StringUtils.isNotBlank(getUserIdByCurrent()) ? Long.valueOf(getUserIdByCurrent()) : null;
    }

    /**
     * 从请求头里面获取用户id，注意不是任何情况都能获取到，需要请求头中带着才可以
     * @return
     */
    public static String getUserIdByCurrentReturnDefault(String defaultUserId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(DefaultConstants.USER_ID);
        }else{
            return defaultUserId;
        }

    }

    public static String getIpAddressByCurrent(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(DefaultConstants.IP_ADDRESS);
        }else{
            return "";
        }
    }

    /**
     * 从请求头里面获取用户id，注意不是任何情况都能获取到，需要请求头中带着才可以
     * @return
     */
    public static String getUserNameByCurrent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return new String(request.getHeader(DefaultConstants.USER_NAME).getBytes(), StandardCharsets.UTF_8);
        }else{
            return "";
        }

    }

    /**
     * 获取当前的sessionKey
     * @return
     */
    public static String getSessionKeyByCurrent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(DefaultConstants.SESSION_KEY);
        }else{
            return null;
        }

    }
}
