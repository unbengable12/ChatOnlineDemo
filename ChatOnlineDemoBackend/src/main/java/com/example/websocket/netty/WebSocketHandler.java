package com.example.websocket.netty;

import com.alibaba.fastjson2.JSON;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.websocket.WebSocketManager;
import com.example.websocket.response.MessageReceive;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@ChannelHandler.Sharable
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    WebSocketManager webSocketManager;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    AccountService accountService;

    /**
     * 通道就绪后触发，一般用于初始化
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入...");
    }

    /**
     * 通道关闭后触发，一般用于释放资源
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开...");
        MessageReceive<String> messageReceive = new MessageReceive<>();
        messageReceive.setMessageType(Const.NEW_LOGOUT);
        Channel channel = ctx.channel();
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attr.get();
        messageReceive.setData(userId);
        webSocketManager.removeContext(ctx.channel());
        webSocketManager.send2Group(JSON.toJSONString(messageReceive), Const.GROUP_ALL);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userID = attr.get();
        stringRedisTemplate.opsForValue().set(Const.WEBSOCKET_HEARTBEAT + userID, "", 6, TimeUnit.SECONDS);
        log.info("来自用户{}的消息：{}", userID, textWebSocketFrame.text());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 认证
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete e) {
            log.info("握手成功...");
            String uri = e.requestUri();
            log.info("uri: {}", uri);
            // 校验token
            String token = getToken(uri);
            if (token == null) {
                ctx.channel().close();
                return;
            }
            webSocketManager.addContext(token, ctx.channel());
            // 推送新用户上线
            MessageReceive<String> messageReceive = new MessageReceive<>();
            messageReceive.setMessageType(Const.NEW_LOGIN);
            Channel channel = ctx.channel();
            Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
            String userID = attr.get();
            messageReceive.setData(accountService.getById(userID).getUsername());
            webSocketManager.send2Group(JSON.toJSONString(messageReceive), Const.GROUP_ALL);
        }
    }

    private String getToken(String uri) {
        if (uri.isEmpty() || uri.indexOf('?') == -1) {
            return null;
        }
        String[] queryParams = uri.split("\\?");
        if (queryParams.length != 2) return null;
        String[] params = queryParams[1].split("=");
        if (params.length != 2) return null;
        return params[1].replaceAll("%20", " ");
    }
}
