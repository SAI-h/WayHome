package com.example.wayhome.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图的边，记录相邻节点与边权值。
 *
 * @param <T> 边权值类型
 * @author WayHome
 */
@Data
@AllArgsConstructor
public class Edge<T> {

    /** 相邻节点 ID */
    private Long nextId;

    /** 边权值 */
    private T value;
}
