package com.callerIdApplication.handler;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WalkieTalkieHandler extends BinaryWebSocketHandler {

    // Mapa dinámico thread-safe para agrupar las sesiones por canal privado
    private final Map<String, Set<WebSocketSession>> channels = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String channel = getChannelParam(session);
        
        if (channel == null || channel.isEmpty()) {
            channel = "default_lobby";
        }

        // Registrar sesión en el canal correspondiente
        channels.computeIfAbsent(channel, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
        session.getAttributes().put("channel", channel);
        
        System.out.println("🎙️ [PTT] Terminal conectada al canal [" + channel + "]. ID de Sesión: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String channel = (String) session.getAttributes().get("channel");
        if (channel == null) return;

        Set<WebSocketSession> activeSessions = channels.get(channel);
        if (activeSessions == null) return;

        byte[] audioBuffer = message.getPayload().array();

        // Retransmisión en tiempo real a todos los miembros del canal privado (excepto al emisor)
        synchronized (activeSessions) {
            for (WebSocketSession activeSession : activeSessions) {
                if (activeSession.isOpen() && !activeSession.getId().equals(session.getId())) {
                    try {
                        activeSession.sendMessage(new BinaryMessage(audioBuffer));
                    } catch (IOException e) {
                        System.err.println("❌ Error de transmisión en canal [" + channel + "]: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String channel = (String) session.getAttributes().get("channel");
        if (channel != null && channels.containsKey(channel)) {
            Set<WebSocketSession> activeSessions = channels.get(channel);
            activeSessions.remove(session);
            
            if (activeSessions.isEmpty()) {
                channels.remove(channel);
            }
        }
        System.out.println("❌ [PTT] Conexión cerrada para la sesión: " + session.getId());
    }

    private String getChannelParam(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null) {
                return UriComponentsBuilder.fromUri(session.getUri())
                        .build()
                        .getQueryParams()
                        .getFirst("channel");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error extrayendo parámetros del URI: " + e.getMessage());
        }
        return null;
    }
}
