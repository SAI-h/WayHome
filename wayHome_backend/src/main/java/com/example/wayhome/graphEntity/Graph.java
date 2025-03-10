package com.example.wayhome.graphEntity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Graph <T> {
    // 点数
    private Long N;

    // 邻接表
    private Map<Long, List<Edge<T>>> G;

    // 最最短路径算法时的路径长度记录
    private T recordDistance;

    // 真实的路径长度数据(最短时间算法, 不包含换乘)
    private Double realDistance;

    // 构造
    public Graph(Long N) {
        this.N = N;
        G = new HashMap<>();
        for(long i = 0; i < N; i++) {
            G.put(i, new ArrayList<>());
        }
    }

    // 插入边
    public void insertEdge(Long startPointID, Long endPointID, T value) {
        assert startPointID >= 0 && startPointID < N : "Invalid startPointID: " + startPointID;
        assert endPointID >= 0 && endPointID < N : "Invalid endPointID: " + endPointID;
        assert value != null : "Value is null";
        G.get(startPointID).add(new Edge<>(endPointID, value));
    }

}
