package com.example.wayhome.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.wayhome.vo.AdminVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 令牌工具，负责生成与校验管理员登录令牌。
 *
 * @author WayHome
 */
@Component
public class JwtAdminConfig {

    /** JWT 签名密钥 */
    @Value("${jwt.secret}")
    private String secretKey;

    /** JWT 过期时间（秒） */
    @Value("${jwt.expiration}")
    private Long expirationSeconds;

    /**
     * 根据管理员信息生成 JWT 令牌。
     *
     * @param adminVO 管理员信息
     * @return JWT 令牌
     */
    public String generateJwtToken(AdminVO adminVO) {
        return JWT.create()
                .withClaim("adminID", adminVO.getAdminID())
                .withClaim("cityID", adminVO.getCity().getCityID())
                .withClaim("cityLat", adminVO.getCity().getCityLat())
                .withClaim("cityLng", adminVO.getCity().getCityLng())
                .withClaim("cityName", adminVO.getCity().getCityName())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 校验 JWT 令牌，校验失败时抛出对应异常。
     *
     * @param token JWT 令牌
     */
    public void verifyToken(String token) {
        JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
    }
}
