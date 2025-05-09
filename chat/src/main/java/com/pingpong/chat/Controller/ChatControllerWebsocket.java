package com.pingpong.chat.Controller;

import com.pingpong.chat.Actors.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ChatControllerWebsocket implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    public ChatControllerWebsocket(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler ,"/ws/chat/roomId={roomId}").setAllowedOrigins("*");
    }
}
