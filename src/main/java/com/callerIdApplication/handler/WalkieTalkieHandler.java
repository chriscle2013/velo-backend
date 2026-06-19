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

    // Mapa dinámico que agrupa las sesiones de usuarios por el nombre de su canal privado
    private final Map<String, Set<WebSocketSession>> channels = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extraemos el parámetro 'channel' enviado desde la app Android en la URL
        String channel = getChannelParam(session);
        
        // Si no se define canal, lo mandamos a uno por defecto (seguridad)
        if (channel == null || channel.isEmpty()) {
            channel = "default_lobby";
        }

        // Agregamos la sesión al canal correspondiente de forma thread-safe
        channels.computeIfAbsent(channel, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
        
        // Guardamos el nombre del canal dentro de los atributos de la sesión para recuperarlo al cerrar
        session.getAttributes().put("channel", channel);
        
        System.out.println("🎙️ [PTT] Dispositivo conectado al canal [" + channel + "]. ID: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // Recuperamos el canal al que pertenece este usuario
        String channel = (String) session.getAttributes().get("channel");
        if (channel == null) return;

        Set<WebSocketSession> activeSessions = channels.get(channel);
        if (activeSessions == null) return;

        byte[] audioBuffer = message.getPayload().array();

        // RETRANSMISIÓN DIRIGIDA: Solo enviamos el audio a los miembros de ESTE canal privado
        synchronized (activeSessions) {
            for (WebSocketSession activeSession : activeSessions) {
                if (activeSession.isOpen() && !activeSession.getId().equals(session.getId())) {
                    try {
                        activeSession.sendMessage(new BinaryMessage(audioBuffer));
                    } catch (IOException e) {
                        System.err.println("Error enviando audio en canal " + channel + ": " + e.getMessage());
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
            
            // Si el canal se queda solo o vacío, limpiamos la memoria eliminándolo del mapa
            if (activeSessions.isEmpty()) {
                channels.remove(channel);
            }
        }
        System.out.println("❌ [PTT] Conexión cerrada para la sesión: " + session.getId());
    }

    // Método utilitario para leer parámetros de la URL del WebSocket
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
            System.err.println("Error extrayendo parámetros de URL: " + e.getMessage());
        }
        return null;
    }
}
