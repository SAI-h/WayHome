package com.example.wayhome.serialization;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 列表序列化包装类，用于解决 Redis 反序列化泛型擦除问题。
 *
 * @param <T> 列表元素类型
 * @author WayHome
 */
@Data
public class ListSerialization<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 列表数据 */
    private List<T> data;
}
