package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Message;
import com.example.entity.vo.request.MessageSendVo;

public interface MessageService extends IService<Message> {
    void sendMessage(MessageSendVo vo);
    String getHistoryMessage(Integer self, Integer other);
    void updateStatus(Integer self, Integer other);
}
