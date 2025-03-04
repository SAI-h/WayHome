package com.example.wayhome.Interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.wayhome.config.JwtAdminConfig;
import com.example.wayhome.exception.BusinessException;
import com.example.wayhome.utils.ResultCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtAdminConfig jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求来源端口,仅对管理员操作进行拦截
        if (!"18080".equals(request.getHeader("Origin").split(":")[2])) {
            return true;
        }
        String jwtToken = request.getHeader("Authorization");
        Map<String, Object> response_message = new HashMap<>();
        try {
            jwtUtils.verifyToken(jwtToken);
            return true;
        } catch (SignatureVerificationException e) {
            response_message.put("code", ResultCodeEnum.Signature_Exception.getCode());
            response_message.put("message", ResultCodeEnum.Signature_Exception.getMessage());
        } catch (TokenExpiredException e) {
            response_message.put("code", ResultCodeEnum.Expired_JwtException.getCode());
            response_message.put("message", ResultCodeEnum.Expired_JwtException.getMessage());
        } catch (AlgorithmMismatchException e) {
            response_message.put("code", ResultCodeEnum.AlgorithmMismatch_Exception.getCode());
            response_message.put("message", ResultCodeEnum.AlgorithmMismatch_Exception.getMessage());
        } catch (Exception e) {
            response_message.put("code", ResultCodeEnum.Jwt_Exception.getCode());
            response_message.put("message", ResultCodeEnum.Jwt_Exception.getMessage() + ": " + e.getMessage());
        }
        String json_response = new ObjectMapper().writeValueAsString(response_message);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json_response);
        return false;
    }
}
