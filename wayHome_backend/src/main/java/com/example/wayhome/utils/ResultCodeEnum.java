package com.example.wayhome.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回结果状态码。
 *
 * <p>状态码遵循分层设计：2xx 表示成功，5xx 表示系统级错误，6xx 表示业务级错误，
 * 7xx 表示认证相关错误，8xx 表示异步任务状态。</p>
 *
 * @author WayHome
 */
@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    /** 操作成功 */
    SUCCESS(200, "success"),

    /** 数据库执行异常 */
    SQL_ERROR(500, "sqlError"),

    /** 登录失败 */
    LOGIN_ERROR(501, "loginFailed"),

    /** 数据插入失败 */
    INSERT_ERROR(601, "insertFailed"),

    /** 数据查询失败 */
    QUERY_ERROR(602, "queryFailed"),

    /** 数据更新失败 */
    UPDATE_ERROR(603, "updateFailed"),

    /** 数据删除失败 */
    DELETE_ERROR(604, "deleteFailed"),

    /** JWT 签名不合法 */
    SIGNATURE_EXCEPTION(701, "Invalid JWT signature"),

    /** JWT 令牌已过期 */
    EXPIRED_JWT_EXCEPTION(702, "Expired JWT token"),

    /** JWT 算法不匹配 */
    ALGORITHM_MISMATCH_EXCEPTION(703, "Algorithm mismatch wrong"),

    /** JWT 令牌错误 */
    JWT_EXCEPTION(704, "JWT token error"),

    /** 任务已接收，等待处理 */
    TASK_RECEIVE(801, "Task received"),

    /** 任务处理中 */
    PROCESSING(802, "PROCESSING");

    /** 状态码 */
    private final Integer code;

    /** 状态信息 */
    private final String message;
}
