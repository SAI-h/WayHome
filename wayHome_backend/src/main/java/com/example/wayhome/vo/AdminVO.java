package com.example.wayhome.vo;

import com.example.wayhome.entity.City;
import lombok.Data;

@Data
public class AdminVO {

    private Long adminID;

    private String account;

    private City city;

}
