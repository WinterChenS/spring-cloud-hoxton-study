package com.winterchen.auth.utils;


import com.winterchen.auth.enums.ResultCodeEnum;
import com.winterchen.auth.exception.BusinessException;
import com.winterchen.auth.interfaces.ResponseCodeInterface;

import java.util.Map;

public class CommonAssert {
	
    public static void meetCondition(boolean condition ,  ResponseCodeInterface responseCodeInterface) {
     
    	if (condition) 
            throw new BusinessException(responseCodeInterface.getCode(), responseCodeInterface.getMessage());
    	
    }
    
    public static void meetCondition(boolean condition ,  ResponseCodeInterface responseCodeInterface , String msg) {
    	meetCondition(condition, responseCodeInterface.getCode(), msg);
    }

    public static void meetCondition(boolean condition , Long code , String msg) {
        if (condition) 
            throw new BusinessException(code , msg);
    }

    public static void meetCondition(boolean condition, String msg) {
    
    	if (condition) 
            throw new BusinessException(ResultCodeEnum.BUSINESS_FAILED.getCode() , msg);
   
    }

    public static void meetCondition(boolean condition , Long code , String msg , Map<String, Object> otherInfo) {
      
    	if (condition) 
            throw new BusinessException(code, msg, otherInfo);
   
    }
    
    public static void meetCondition(boolean condition , ResponseCodeInterface responseCodeInterface ,  Map<String, Object> otherInfo) {

    	if (condition) 
            throw new BusinessException(responseCodeInterface.getCode() , responseCodeInterface.getMessage() , otherInfo);
   
    }
    
}
