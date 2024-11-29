package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.Data;

import java.util.Date;

@TableName(value = "db_message")
@Data
public class Message implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    String content;
    Integer sid;
    Integer rid; // 0：所有人
    Date sendTime;
    Integer status; // 0.未读 1.已读
}