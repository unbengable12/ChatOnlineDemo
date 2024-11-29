package com.example.config;

import com.example.websocket.netty.NettyWebSocketStarter;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class MyApplicationRunner {
    @Resource
    NettyWebSocketStarter socketStarter;
    @Bean
    public ApplicationRunner runner() {
        return args -> {
            new Thread(socketStarter).start();
        };
    }
}
