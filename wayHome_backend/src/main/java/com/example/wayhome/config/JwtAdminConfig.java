package com.example.wayhome.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.wayhome.vo.AdminVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class JwtAdminConfig {

    @Value("${jwt.secret}")
    private String SECRET_KEY ;

    @Value("${jwt.expiration}")
    private Long Expiration;

    /*
     * 生成token
     */
    public String generateJwtToken(AdminVO adminVO) {
        return JWT.create()
                .withClaim("adminID", adminVO.getAdminID())
                .withClaim("cityID", adminVO.getCity().getCityID())
                .withClaim("cityLat", adminVO.getCity().getCityLat())
                .withClaim("cityLng", adminVO.getCity().getCityLng())
                .withClaim("cityName", adminVO.getCity().getCityName())
                .withExpiresAt(new Date(System.currentTimeMillis() + Expiration * 1000))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * 解析JWT Token并提取信息
     * @param token JWT Token
     */
    public void verifyToken(String token) {
        JWT.require(Algorithm.HMAC256(SECRET_KEY)).build().verify(token);
    }


}
