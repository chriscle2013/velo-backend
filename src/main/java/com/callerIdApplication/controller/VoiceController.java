package com.callerIdApplication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/voice")
@CrossOrigin(origins = "*") // 🔥 ESTO EVITA EL ERROR DE CONEXIÓN
public class VoiceController {

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeVoice(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String audioBase64 = payload.get("audio");
            if (audioBase64 == null) {
                response.put("status", "error");
                return ResponseEntity.badRequest().body(response);
            }
            
            byte[] audioData = Base64.getDecoder().decode(audioBase64);

            // 🧠 MOTOR DE DETECCIÓN VELO IA v1.1
            boolean isSynthetic = detectAiArtifacts(audioData);

            response.put("status", "success");
            response.put("isAi", isSynthetic);
            response.put("verdict", isSynthetic ? "ALERTA IA: Voz Sintética" : "HUMANO: Voz Orgánica");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private boolean detectAiArtifacts(byte[] data) {
        if (data.length < 1000) return false;
        
        // Buscamos patrones de repetición matemática que las IAs 
        // de clonación suelen dejar en los paquetes de datos
        int identicalMatches = 0;
        for (int i = 0; i < data.length - 10; i++) {
            if (data[i] == data[i+1] && data[i] == data[i+2]) {
                identicalMatches++;
            }
        }
        // Un audio humano tiene mucho ruido "sucio" (aleatorio), 
        // una IA tiene patrones mucho más limpios y repetitivos.
        return identicalMatches > (data.length * 0.15); 
    }
}
