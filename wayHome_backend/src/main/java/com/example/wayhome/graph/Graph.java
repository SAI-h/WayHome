package com.example.wayhome.graph;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邻接表实现的图。
 *
 * @param <T> 边权值类型
 * @author WayHome
 */
@Data
public class Graph<T> {

    /** 节点数量 */
    private Long nodeCount;

    /** 邻接表 */
    private Map<Long, List<Edge<T>>> adjacencyList;

    /** 最短路径算法得到的路径长度 */
    private T recordDistance;

    /** 真实路径长度（最短时间算法，不包含换乘） */
    private Double realDistance;

    /**
     * 构造指定节点数量的图。
     *
     * @param nodeCount 节点数量
     */
    public Graph(Long nodeCount) {
        this.nodeCount = nodeCount;
        this.adjacencyList = new HashMap<>();
        for (long i = 0; i < nodeCount; i++) {
            adjacencyList.put(i, new ArrayList<>());
        }
    }

    /**
     * 插入一条边。
     *
     * @param startPointId 起点节点 ID
     * @param endPointId   终点节点 ID
     * @param value        边权值
     */
    public void insertEdge(Long startPointId, Long endPointId, T value) {
        if (startPointId == null || endPointId == null || value == null) {
            throw new IllegalArgumentException("边参数不允许为空");
        }
        if (startPointId < 0 || startPointId >= nodeCount
                || endPointId < 0 || endPointId >= nodeCount) {
            throw new IllegalArgumentException("节点 ID 越界: " + startPointId + " -> " + endPointId);
        }
        adjacencyList.get(startPointId).add(new Edge<>(endPointId, value));
    }
}
