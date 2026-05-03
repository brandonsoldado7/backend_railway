package neuro_activity.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import neuro_activity.backend.service.IAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IAController {

    @Autowired
    private IAService iaService;

    // POST /api/ia/generar
    @PostMapping("/generar")
    public ResponseEntity<?> generarDiagrama(@RequestBody Map<String, String> body) {
        try {
            String prompt = body.get("prompt");
            JsonNode resultado = iaService.generarDiagrama(prompt);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ia/modificar
    @PostMapping("/modificar")
    public ResponseEntity<?> modificarDiagrama(@RequestBody Map<String, Object> body) {
        try {
            String prompt = (String) body.get("prompt");
            Object jsonActual = body.get("jsonActual");
            JsonNode resultado = iaService.modificarDiagrama(prompt, jsonActual);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ia/analizar-cuellos
    @PostMapping("/analizar-cuellos")
    public ResponseEntity<?> analizarCuellos(@RequestBody Map<String, String> body) {
        try {
            String base64Image = body.get("base64Image");
            JsonNode resultado = iaService.analizarCuellosDesdeImagen(base64Image);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}