package com.example.websocket.response;

import lombok.Data;

@Data
public class MessageReceive<V> {
    String messageType;
    V data;
}
