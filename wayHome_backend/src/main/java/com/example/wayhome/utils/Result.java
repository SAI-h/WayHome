package com.example.wayhome.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一返回结果封装。
 *
 * @param <T> 数据负载类型
 * @author WayHome
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 业务状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 数据负载 */
    private T data;

    /**
     * 按自定义状态码构建返回结果。
     *
     * @param data    数据负载
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 返回结果
     */
    public static <T> Result<T> build(T data, Integer code, String message) {
        return new Result<>(code, message, data);
    }

    /**
     * 按状态码枚举构建返回结果。
     *
     * @param data            数据负载
     * @param resultCodeEnum  状态码枚举
     * @param <T>             数据类型
     * @return 返回结果
     */
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        return new Result<>(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    /**
     * 构建成功返回结果。
     *
     * @param data 数据负载
     * @param <T>  数据类型
     * @return 成功返回结果
     */
    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 构建失败返回结果。
     *
     * @param resultCodeEnum 失败状态码枚举
     * @param <T>            数据类型
     * @return 失败返回结果
     */
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum) {
        return build(null, resultCodeEnum);
    }

    /**
     * 构建失败返回结果。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败返回结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return build(null, code, message);
    }
}
