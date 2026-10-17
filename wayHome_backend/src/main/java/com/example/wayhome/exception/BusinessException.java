package com.example.wayhome.exception;

import com.example.wayhome.utils.ResultCodeEnum;
import lombok.Getter;

/**
 * 业务异常，用于在业务处理过程中中断流程并携带统一状态码。
 *
 * @author WayHome
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final Integer code;

    /**
     * 按状态码枚举构建业务异常。
     *
     * @param resultCodeEnum 状态码枚举
     */
    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    /**
     * 按自定义状态码与提示信息构建业务异常。
     *
     * @param code    状态码
     * @param message 提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
