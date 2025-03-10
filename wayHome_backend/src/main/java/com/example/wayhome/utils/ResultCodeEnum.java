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
    SQL_ERROR(500, "sqlError"),
    LOGIN_ERROR(501,"loginFailed"),
    INSERT_ERROR(601, "insertFailed"),
    QUERY_ERROR(602, "queryFailed"),
    UPDATE_ERROR(603, "updateFailed"),
    DELETE_ERROR(604, "deleteFailed"),
    Signature_Exception(701, "Invalid JWT signature"),
    Expired_JwtException(702, "Expired JWT token"),
    AlgorithmMismatch_Exception(703, "Algorithm mismatch wrong"),
    Jwt_Exception(704, "JWT token error"),
    TASK_RECEIVE(801, "Task received"),
    PROCESSING(802, "PROCESSING");

    private final Integer code;
    private final String message;
}
