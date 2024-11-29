package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {
    Integer selectUnreadCount(@Param("self") Integer self, @Param("other") Integer other);
    List<Message> selectMessage(@Param("self" ) Integer self, @Param("other") Integer other);
}




