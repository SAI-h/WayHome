package com.example.wayhome.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.wayhome.config.JwtAdminConfig;
import com.example.wayhome.utils.ResultCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证拦截器，仅对管理端请求进行身份校验。
 *
 * <p>管理端运行于 18080 端口，通过 Origin 请求头判断来源；
 * 用户端请求直接放行。</p>
 *
 * @author WayHome
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** 管理端来源端口 */
    private static final String ADMIN_ORIGIN_PORT = "18080";

    /** 认证请求头名称 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtAdminConfig jwtAdminConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtInterceptor(JwtAdminConfig jwtAdminConfig) {
        this.jwtAdminConfig = jwtAdminConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!isAdminRequest(request)) {
            return true;
        }

        String jwtToken = request.getHeader(AUTHORIZATION_HEADER);
        try {
            jwtAdminConfig.verifyToken(jwtToken);
            return true;
        } catch (SignatureVerificationException e) {
            writeErrorResponse(response, ResultCodeEnum.SIGNATURE_EXCEPTION);
        } catch (TokenExpiredException e) {
            writeErrorResponse(response, ResultCodeEnum.EXPIRED_JWT_EXCEPTION);
        } catch (AlgorithmMismatchException e) {
            writeErrorResponse(response, ResultCodeEnum.ALGORITHM_MISMATCH_EXCEPTION);
        } catch (Exception e) {
            log.warn("JWT 令牌校验失败: {}", e.getMessage());
            writeErrorResponse(response, ResultCodeEnum.JWT_EXCEPTION);
        }
        return false;
    }

    /**
     * 判断请求是否来自管理端。
     *
     * @param request HTTP 请求
     * @return 是否来自管理端
     */
    private boolean isAdminRequest(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            return false;
        }
        String[] segments = origin.split(":");
        return segments.length > 2 && ADMIN_ORIGIN_PORT.equals(segments[2]);
    }

    /**
     * 写出统一的错误响应。
     *
     * @param response       HTTP 响应
     * @param resultCodeEnum 状态码枚举
     * @throws Exception 写出异常
     */
    private void writeErrorResponse(HttpServletResponse response, ResultCodeEnum resultCodeEnum)
            throws Exception {
        Map<String, Object> body = new HashMap<>(4);
        body.put("code", resultCodeEnum.getCode());
        body.put("message", resultCodeEnum.getMessage());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
