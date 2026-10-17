package com.example.wayhome.exception;

import com.example.wayhome.utils.Result;
import com.example.wayhome.utils.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一拦截业务异常、参数校验异常与未知异常。
 *
 * @author WayHome
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 统一返回结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 {@link MethodArgumentNotValidException} 参数校验异常。
     *
     * @param e 参数校验异常
     * @return 统一返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = extractFieldErrorMessage(e.getBindingResult().getFieldError());
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCodeEnum.QUERY_ERROR.getCode(), message);
    }

    /**
     * 处理 {@link BindException} 参数绑定异常。
     *
     * @param e 参数绑定异常
     * @return 统一返回结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result<Void> handleBindException(BindException e) {
        String message = extractFieldErrorMessage(e.getBindingResult().getFieldError());
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCodeEnum.QUERY_ERROR.getCode(), message);
    }

    /**
     * 处理未知异常，避免将堆栈信息直接暴露给调用方。
     *
     * @param e 未知异常
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCodeEnum.SQL_ERROR.getCode(), "系统繁忙，请稍后重试");
    }

    /**
     * 从字段错误中提取可读的错误信息。
     *
     * @param fieldError 字段错误
     * @return 错误信息
     */
    private String extractFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return "请求参数不合法";
        }
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
