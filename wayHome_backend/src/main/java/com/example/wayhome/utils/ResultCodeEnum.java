package com.example.wayhome.utils;

import com.sun.net.httpserver.Authenticator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 统一返回结果状态信息
 */
@Getter
@AllArgsConstructor
@ToString
public enum ResultCodeEnum {
    SUCCESS(200,"success"),
    LOGIN_ERROR(501,"loginFailed");

    private Integer code;
    private String message;
}
