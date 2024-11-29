package com.example.websocket;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketManager {
    private static final Map<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    private static final Map<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    JwtUtils jwtUtils;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    public void addContext(String token, Channel channel) {
        String userId = getUserId(token);
        if (userId == null) {
            channel.close();
            return;
        }
        String channelId = channel.id().toString();
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        USER_CONTEXT_MAP.put(userId, channel);
        // 加入所有人聊天组
        add2Group(Const.GROUP_ALL, channel);
        // 立马更新心跳
        stringRedisTemplate.opsForValue().set(Const.WEBSOCKET_HEARTBEAT + userId, "", 6, TimeUnit.SECONDS);
    }

    private String getUserId(String token) {
        DecodedJWT decodedJWT = jwtUtils.resolveJwt(token);
        if (decodedJWT == null) return null;
        return jwtUtils.toId(decodedJWT).toString();
    }

    public void sendMessage(String message, String receiver) {
        if (receiver == null) return;
        Channel channel = USER_CONTEXT_MAP.get(receiver);
        if (channel == null) return;
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    public void send2Group(String message, String group) {
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(group);
        if (channelGroup == null) return;
        channelGroup.writeAndFlush(new TextWebSocketFrame(message));
    }

    public void removeContext(Channel channel) {
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attr.get();
        if (userId != null) {
            USER_CONTEXT_MAP.remove(userId);
            // 立马停止心跳
            stringRedisTemplate.delete(Const.WEBSOCKET_HEARTBEAT + userId);
        }
    }

    private void add2Group(String group, Channel channel) {
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(group);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(group, channelGroup);
        }
        if (channel == null) {
            return;
        }
        channelGroup.add(channel);
    }

    public List<Integer> getOnlineUserIds() {
        return new ArrayList<>(USER_CONTEXT_MAP.keySet()).stream().map(Integer::parseInt).toList();
    }
}
