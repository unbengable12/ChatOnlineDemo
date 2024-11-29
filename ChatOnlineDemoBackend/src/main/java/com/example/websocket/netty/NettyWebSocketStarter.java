package com.example.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// 注册为Bean @Component
// 设置init方法为异步方法 @Async
// 在启动Springboot时，调用init方法
@Component
@Slf4j
public class NettyWebSocketStarter implements Runnable {
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Resource
    HeartBeatHandler heartBeatHandler;
    @Resource
    WebSocketHandler webSocketHandler;
    @Value("${websocket.port}")
    Integer port;
    @Value("${websocket.heartbeat-timeout}")
    Long heartbeatTimeout;
    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    // 对 http 协议支持
                    pipeline.addLast(new HttpServerCodec());
                    // 聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                    // 保证接受的 http 请求完整
                    pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                    // 心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                    // readerIdleTime 读超时时间，即测试端一定时间内未接收到测试端发来消息
                    // writerIdleTime 写超时时间，即测试端一定时间内想被发送消息
                    // allIdleTime 所有超时时间
                    // 可以存储在 redis 中，用于记录用户的连接状态
                    // 防止重复登录，在线用户列表
                    pipeline.addLast(new IdleStateHandler(heartbeatTimeout, 0, 0, TimeUnit.SECONDS));
                    pipeline.addLast(heartBeatHandler);
                    // 将http协议升级为websocket协议
                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L));
                    pipeline.addLast(webSocketHandler);
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("netty 启动成功");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动 netty 失败{}", e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
