package com.example.wayhome.exception;

import com.example.wayhome.utils.Result;
import com.example.wayhome.utils.ResultCodeEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> BusinessExceptionHandler(BusinessException businessException) {
        return Result.build(null, businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public Result<?> handleSQLException(SQLException sqlException) {
        return Result.build(null, ResultCodeEnum.SQL_ERROR.getCode(),
                ResultCodeEnum.SQL_ERROR.getMessage() + ":" + sqlException);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(Exception bex) {
        return Result.build(null, null, bex.toString());
    }

}
