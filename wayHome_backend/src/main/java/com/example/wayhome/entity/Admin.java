package com.example.wayhome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin")
public class Admin {

    @TableId(value = "adminID", type = IdType.AUTO)
    private Long adminID;

    @TableField("account")
    private String account;

    @TableField("password")
    private String password;

    @TableField(exist = false)
    private City city;

}
