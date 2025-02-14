package com.example.openai.config;


import com.example.openai.Service.KimiWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 端点，确保路径是 /chat
        registry.addHandler(new KimiWebSocketHandler(), "/chat")
                .setAllowedOrigins("*"); // 允许所有来源，或者指定允许的来源
    }
}
