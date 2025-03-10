package com.example.wayhome.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局返回统一的结果类
 */
@Data
@AllArgsConstructor // 生成含所有字段的构造函数
@NoArgsConstructor // 生成无参构造函数
public class Result <T> {

    private Integer code;

    private String message;

    private T data;

    public static <T> Result<T> build(T data, Integer code, String message) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        return new Result<>(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 仅返回带有code的Result
     */
    private Result<T> code(Integer code) {
        setCode(code);
        return this;
    }

    /**
     * 仅返回带有message的Result
     */
    private Result<T> message(String message) {
        setMessage(message);
        return this;
    }

}
