package com.example.wayhome.service.impl;

import org.springframework.stereotype.Service;

@Service
public class PathCalculateServiceImpl {

    // 获取当前时间段还在运行的路线和班次

    // 根据正在运行的班次加载所有的站点和控制点信息

    // 建图,邻接表 (班次决定方向, 站点和路线为一个整体作为为点, 点和点之间的距离为相邻两个点之间与控制点关联的路径和)
    // 加边操作分为两种: 同一线路上的两个不同站点之间(物理距离 / 公交车运行时速), 同一个站点的两条线路之间(平均等候时长)

    // 做记录方案的最短路径操作...

}
