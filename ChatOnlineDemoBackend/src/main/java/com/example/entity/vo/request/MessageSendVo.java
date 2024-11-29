package com.example.entity.vo.request;

import com.example.entity.BaseData;
import lombok.Data;

@Data
public class MessageSendVo implements BaseData {
    String content;
    Integer rid;
    Integer sid;
}
