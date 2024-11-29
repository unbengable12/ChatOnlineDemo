package com.example.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Message;
import com.example.entity.vo.request.MessageSendVo;
import com.example.mapper.MessageMapper;
import com.example.service.MessageService;
import com.example.utils.Const;
import com.example.websocket.WebSocketManager;
import com.example.websocket.response.MessageReceive;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author sun
 * @description 针对表【db_message】的数据库操作Service实现
 * @createDate 2024-11-27 16:39:36
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    WebSocketManager webSocketManager;

    @Override
    public void sendMessage(MessageSendVo vo) {
        Message message = vo.asViewObject(Message.class, m -> {
            m.setSendTime(new Date());
            m.setStatus(0);
        });
        this.save(message);
        // ws 推送
        MessageReceive<Message> messageReceive = new MessageReceive<>();
        messageReceive.setMessageType(Const.NEW_MESSAGE);
        messageReceive.setData(message);
        if (vo.getRid() == 0)
            webSocketManager.send2Group(JSON.toJSONString(messageReceive), Const.GROUP_ALL);
        else
            webSocketManager.sendMessage(JSON.toJSONString(messageReceive), vo.getRid().toString());
    }

    @Override
    public String getHistoryMessage(Integer self, Integer other) {
        List<Message> messages = this.baseMapper.selectMessage(self, other);
        return JSON.toJSONString(messages);
    }

    @Override
    public void updateStatus(Integer self, Integer other) {
        UpdateWrapper<Message> wrapper = new UpdateWrapper<>();
        wrapper.eq("sid", other);
        wrapper.eq("rid", self);
        wrapper.set("status", 1);
        this.update(wrapper);
    }
}




