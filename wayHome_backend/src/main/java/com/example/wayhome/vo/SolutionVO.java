package com.example.wayhome.vo;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SolutionVO <T> {

    private T distance;

    private Double realDistance;

    private List<List<StationInRouteVO>> scheme = new ArrayList<>();

    private List<List<PointTransferVO>> routeDetail = new ArrayList<>();

}
