package com.example.wayhome.vo;

import lombok.Data;
import org.springframework.data.geo.Point;

@Data
public class PointTransferVO {

    private Long staID;

    private Point point;

}
