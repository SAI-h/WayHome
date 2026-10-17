package com.example.wayhome.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 最短路径算法中的节点，记录 A* 算法的 g 值与 f 值。
 *
 * @author WayHome
 */
@Data
@AllArgsConstructor
public class Node {

    /** 节点 ID */
    private long id;

    /** 起点到当前节点的真实代价 */
    private double gScore;

    /** 估计的总代价 */
    private double fScore;
}
