package com.winterchen.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 *
 * <ol>
 * <li>iss: jwt签发者</li>
 * <li>sub: jwt所面向的用户</li>
 * <li>aud: 接收jwt的一方</li>
 * <li>exp: jwt的过期时间，这个过期时间必须要大于签发时间</li>
 * <li>nbf: 定义在什么时间之前，该jwt都是不可用的</li>
 * <li>iat: jwt的签发时间</li>
 * <li>jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击</li>
 * </ol>
 *
 * @Description: JWT校验工具类
 * @Author: winterchen
 * @Date: 2019-06-12
 *
 */
public class JwtUtil {
    private static Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    //过期时间
    private static Long timeLimit = 30 * 24 * 60 * 60 * 1000L;



    /**
      *
      * @Description: 创建token
      * @Author: winterchen
      * @Date: 2019-06-12
      *
    */
    public static String createToken(String subject, Map<String, Object> map, String secretKey){
        try {
            byte[] bytes = Base64.getEncoder().encode(secretKey.getBytes("utf-8"));
            return createToken(subject, map, bytes);
        } catch (Exception e) {
            logger.error("createToken error",e);
        }
        return null;
    }


    /**
     * 包含超时
     * @param subject
     * @param map
     * @param secretKey
     * @param timeLimit
     * @return
     */
    public static String createToken(String subject, Map<String, Object> map, String secretKey,Long timeLimit){
        try {
            byte[] bytes = Base64.getEncoder().encode(secretKey.getBytes("utf-8"));
            return createToken(subject, map, bytes, timeLimit);
        } catch (Exception e) {
            logger.error("createToken error",e);
        }
        return null;
    }

    /**
     * 包含超时
     * @param subject
     * @param map
     * @param secretKey
     * @param validStartDate 有效开始时间
     * @param validEndDate 有效结束时间
     * @return
     */
    public static String createToken(String subject, Map<String, Object> map, String secretKey,Date validStartDate ,Date validEndDate){
        try {
            byte[] bytes = Base64.getEncoder().encode(secretKey.getBytes("utf-8"));
            return createToken(subject, map, bytes, validStartDate, validEndDate);
        } catch (Exception e) {
            logger.error("createToken error",e);
        }
        return null;
    }

    /**
     *
     * @Description: 创建token
     * @Author: winterchen
     * @Date: 2019-06-12
     *
     */
    private static String createToken(String subject, Map<String, Object> map, byte[] secretKey) {
        String userToken = null;
        Long nowTime=System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder().setSubject(subject)
                .setExpiration(new Date(nowTime + timeLimit))
                .setNotBefore(new Date(nowTime));

        if (map != null) {
            for (String key : map.keySet()) {
                builder.claim(key, map.get(key));
            }
        }
        userToken = builder.signWith(SignatureAlgorithm.HS512, secretKey).compact();

        return userToken;
    }

    /**
     *
     * @Description: 创建token
     * @Author: winterchen
     * @Date: 2019-06-12
     *
     */
    private static String createToken(String subject, Map<String, Object> map, byte[] secretKey,Long timeLimit) {
        String userToken = null;
        Long nowTime=System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder().setSubject(subject)
                .setExpiration(new Date(nowTime + timeLimit)).setNotBefore(new Date(nowTime));
        if (map != null) {
            for (String key : map.keySet()) {
                builder.claim(key, map.get(key));
            }
        }
        userToken = builder.signWith(SignatureAlgorithm.HS512, secretKey).compact();

        return userToken;
    }

    private static String createToken(String subject, Map<String, Object> map, byte[] secretKey,Date validStartDate ,Date validEndDate) {
        String userToken = null;
        JwtBuilder builder = Jwts.builder().setSubject(subject)
                .setExpiration(validEndDate).setNotBefore(validStartDate);

        if (map != null) {
            for (String key : map.keySet()) {
                builder.claim(key, map.get(key));
            }
        }
        userToken = builder.signWith(SignatureAlgorithm.HS512, secretKey).compact();

        return userToken;
    }

    /**
     * 解密 jwt
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt, String secretKey) throws Exception {
        byte[] bytes = Base64.getEncoder().encode(secretKey.getBytes("utf-8"));
        Claims claims = Jwts.parser().setSigningKey(bytes).parseClaimsJws(jwt).getBody();
        return claims;
    }
}