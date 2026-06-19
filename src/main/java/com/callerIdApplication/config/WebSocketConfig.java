package com.callerIdApplication.config; // Verifica el paquete de tu carpeta config

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// ⚡ IMPORTACIÓN EXACTA ACTUALIZADA:
import com.callerIdApplication.handler.WalkieTalkieHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WalkieTalkieHandler(), "/walkietalkie")
                .setAllowedOrigins("*");
    }
}
